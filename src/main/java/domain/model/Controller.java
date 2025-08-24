package domain.model;

import java.util.List;

import infrastructure.api.Route;

public interface Controller {
    List<Route> getRoutes();
}
