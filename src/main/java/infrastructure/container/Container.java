package infrastructure.container;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import infrastructure.container.exceptions.ContainerException;
import infrastructure.server.Server;

public class Container {
    private static final Logger logger = LogManager.getLogger(Container.class);
    private static final Map<String, Object> instances = new HashMap<>();
    private static final String ERROR_INSTANTIATION = "Failed to instantiate class: %s";

    private Container() {
        // Private constructor to prevent instantiation
    }

    public static <T> T register(Class<T> type, Object... args) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        return (T) register(type.getName(), type, args);
    }

    public static <T> T register(String name, Class<T> type, Object... args) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        T instance = createInstance(type, args);
        instances.put(name, instance);
        logger.info("Registered instance of {} with name {}", type.getName(), name);
        return instance;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(Class<T> type, Object... args) {
        return (T) instances.computeIfAbsent(
                type.getName(),
                k -> createInstance(type, args));
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getInstance(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        return instances.entrySet().stream()
                .filter(entry -> type.isAssignableFrom(entry.getValue().getClass()))
                .map(entry -> (T) entry.getValue())
                .findFirst();
    }

    public static <T> Optional<T> getInstance(String name, Class<T> type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        Object instance = instances.get(name);
        if (instance != null && type.isInstance(instance)) {
            return Optional.of(type.cast(instance));
        }
        return Optional.empty();
    }

    private static <T> T createInstance(Class<T> type, Object... args) {
        try {
            Class<?>[] argTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);

            Constructor<T> constructor = type.getDeclaredConstructor(argTypes);
            return constructor.newInstance(args);
        } catch (Exception e) {
            String errorMsg = String.format(ERROR_INSTANTIATION, type.getName());
            logger.error(errorMsg, e);
            throw new ContainerException(errorMsg, e);
        }
    }

    public static void clear() {
        instances.clear();
        logger.debug("Cleared all registered instances in the container");
    }

    public static List<String> listRegisteredNames() {
        return List.copyOf(instances.keySet());
    }

    public static Server getServerInstance() {
        return Container.getInstance(Server.class)
                .orElseThrow(() -> new ContainerException("Server instance is not registered in the container"));
    }
}
