package pro.velovec.inferno.reborn.worldd.constants;

public class WorldSize {

    public static final int GRID_MAX_COUNT = 8;
    public static final float GRID_SIZE = 128.0f;

    public static final int CELL_MAX_COUNT = 8;
    public static final float CELL_SIZE = GRID_SIZE / CELL_MAX_COUNT;

    public static final int CELL_TOTAL = GRID_MAX_COUNT * CELL_MAX_COUNT;

    public static final int CENTER_GRID_ID = GRID_MAX_COUNT / 2;
    public static final float CENTER_GRID_OFFSET = GRID_SIZE / 2f;

    public static final int CENTER_CELL_ID = CELL_MAX_COUNT * GRID_MAX_COUNT / 2;
    public static final float CENTER_CELL_OFFSET = CELL_SIZE / 2f;

    public static final float MAP_SIZE = GRID_MAX_COUNT * GRID_SIZE;
    public static final float MAP_HALFSIZE = MAP_SIZE / 2f;

    public static final float MAX_SPEED = 1.0f;

    public static final float INNER_INTEREST_AREA_RADIUS = CELL_SIZE * 1.5f;
    public static final float OUTER_INTEREST_AREA_RADIUS = CELL_SIZE * 2.5f;

    private WorldSize() {
        // Prevent class instantiation
    }
}
