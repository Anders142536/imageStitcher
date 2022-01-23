package imageStitcher;

import java.io.File;

public class Tile {
    final File file;
    final int posX;
    final int posY;

    public Tile (File file, int posX, int posY) {
        this.file = file;
        this.posX = posX;
        this.posY = posY;
    }
}
