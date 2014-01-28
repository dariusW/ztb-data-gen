package pl.edu.agh.dynamic.map.model;

import org.postgis.Polygon;

/**
 * Created with IntelliJ IDEA.
 * User: Khajiit
 * Date: 25.01.14
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class Crossroad {

    private Long id;

    private String name;

    private Polygon boundary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Polygon getBoundary() {
        return boundary;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
