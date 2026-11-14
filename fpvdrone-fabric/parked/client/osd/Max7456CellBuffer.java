package com.fpvdrone.client.osd;

public final class Max7456CellBuffer {
    public static final int COLS = 30;
    public static final int ROWS = 16;
    public static final char EMPTY = '\u0000';
    private final char[][] cells = new char[16][30];
    private final float[][] offsetX = new float[16][30];
    private final float[][] offsetY = new float[16][30];

    public void clear() {
        for (int r = 0; r < 16; r++) {
            for (int c = 0; c < 30; c++) {
                this.cells[r][c] = 0;
                this.offsetX[r][c] = 0.0F;
                this.offsetY[r][c] = 0.0F;
            }
        }
    }

    public void writeChar(int col, int row, char ch, float subX, float subY) {
        if (row >= 0 && row < 16 && col >= 0 && col < 30) {
            if (ch != ' ') {
                this.cells[row][col] = ch;
                this.offsetX[row][col] = subX;
                this.offsetY[row][col] = subY;
            }
        }
    }

    public void writeChar(int col, int row, char ch) {
        this.writeChar(col, row, ch, 0.0F, 0.0F);
    }

    public void writeString(float gridX, float gridY, String text) {
        if (text != null) {
            int baseCol = (int)Math.floor(gridX);
            int baseRow = (int)Math.floor(gridY);
            float subX = gridX - baseCol;
            float subY = gridY - baseRow;

            for (int i = 0; i < text.length(); i++) {
                this.writeChar(baseCol + i, baseRow, text.charAt(i), subX, subY);
            }
        }
    }

    public char getChar(int col, int row) {
        return row >= 0 && row < 16 && col >= 0 && col < 30 ? this.cells[row][col] : '\u0000';
    }

    public float getOffsetX(int col, int row) {
        return row >= 0 && row < 16 && col >= 0 && col < 30 ? this.offsetX[row][col] : 0.0F;
    }

    public float getOffsetY(int col, int row) {
        return row >= 0 && row < 16 && col >= 0 && col < 30 ? this.offsetY[row][col] : 0.0F;
    }

    public Max7456CellBuffer snapshot() {
        Max7456CellBuffer copy = new Max7456CellBuffer();

        for (int r = 0; r < 16; r++) {
            System.arraycopy(this.cells[r], 0, copy.cells[r], 0, 30);
            System.arraycopy(this.offsetX[r], 0, copy.offsetX[r], 0, 30);
            System.arraycopy(this.offsetY[r], 0, copy.offsetY[r], 0, 30);
        }

        return copy;
    }
}
