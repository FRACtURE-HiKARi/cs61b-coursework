package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Arrays;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    public static Random RANDOM = new Random();
    public static TETile randomTile(){
        return switch (RANDOM.nextInt(4)) {
            case 0 -> Tileset.FLOWER;
            case 1 -> Tileset.WALL;
            case 2 -> Tileset.GRASS;
            case 3 -> Tileset.FLOOR;
            default -> Tileset.NOTHING;
        };
    }
    public static void fillAll(TETile[][] world, TETile tile) {
        for (TETile[] teTiles : world) {
            Arrays.fill(teTiles, tile);
        }
    }

    public static void main(String[] args) {
        int worldSize = 45;
        TERenderer renderer = new TERenderer();
        renderer.initialize(worldSize, worldSize);
        TETile[][] world = new TETile[worldSize][worldSize];
        fillAll(world, Tileset.NOTHING);
        HexagonChunks chunks = new HexagonChunks(3, 4, 1, 1);
        chunks.showOn(world);
        renderer.renderFrame(world);
    }

    public static class Hexagon{
        int x;
        int y;
        int size;
        TETile tile;
        public Hexagon(int x, int y, int size, TETile tile) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.tile = tile;
        }

        public void showOn(TETile[][] world){
            int maxWidth = 3*size - 2;
            // upper half
            for (int i = 0; i < size; i++) {
                int ident = size - i - 1;
                for (int j = 0; j < maxWidth; j++) {
                    if (j >= ident && j < maxWidth - ident) {
                        world[x+j][y+i] = tile;
                    }
                }
            }
            // lower half
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < maxWidth; j++) {
                    if (j >= i && j < maxWidth - i) {
                        world[x+j][y+i+size] = tile;
                    }
                }
            }
        }
    }

    public static class HexagonColumn{
        Hexagon[] hexagons;
        int x;
        int y;
        int hexagonSize;
        public HexagonColumn(int x, int y, int hexagonCount, int hexagonSize) {
            hexagons = new Hexagon[hexagonCount];
            this.x = x;
            this.y = y;
            this.hexagonSize = hexagonSize;
        }
        public void fillRandom(){
            for (int i = 0; i < hexagons.length; i++) {
                int yOffset = 2*i*hexagonSize;
                hexagons[i] = new Hexagon(x, y+yOffset, hexagonSize, randomTile());
            }
        }
        public void showOn(TETile[][] world){
            for (Hexagon hexagon : hexagons) {
                hexagon.showOn(world);
            }
        }
    }

    public static class HexagonChunks{
        int size;
        int chunkSize;
        int xOffset;
        int yOffset;
        HexagonColumn[] columns;

        public HexagonChunks(int size, int chunkSize, int xOffset, int yOffset){
            this.size = size;
            this.chunkSize = chunkSize;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.columns = new HexagonColumn[2*size - 1];
            initializeColumns();
            fillAllChunks();
        }

        private void initializeColumns(){
            // Calculate column offsets
            int[] columnXOffsets = new int[2*size - 1];
            int[] columnYOffsets = new int[2*size - 1];
            int[] columnHexagonCount = new int[2*size - 1];
            for (int i = 0; i < 2*size - 1; i++) {
                columnXOffsets[i] = xOffset + i * (2*chunkSize - 1);
            }
            for (int i = 0; i < size; i++) {
                columnYOffsets[i] = yOffset + (size - i - 1) * chunkSize;
                columnHexagonCount[i] = size + i;
            }
            for (int i = size; i < 2*size - 1; i++) {
                columnYOffsets[i] = yOffset + (i - size + 1) * chunkSize;
                columnHexagonCount[i] = 3*size - i - 2;
            }
            for (int i = 0; i < 2*size - 1; i++) {
                this.columns[i] = new HexagonColumn(
                        columnXOffsets[i],
                        columnYOffsets[i],
                        columnHexagonCount[i],
                        chunkSize
                );
            }
        }

        private void fillAllChunks(){
            // for each column
            for (HexagonColumn column : columns) {
                column.fillRandom();
            }
        }

        public void showOn(TETile[][] world){
            for (HexagonColumn column : columns) {
                column.showOn(world);
            }
        }
    }


}
