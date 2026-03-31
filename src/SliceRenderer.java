import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class SliceRenderer {
    private VolumeData volumeData;
    private float[][][] grey;
    private int dim;

    public SliceRenderer(VolumeData volumeData) {
        this.volumeData = volumeData;
        this.grey = volumeData.getGreyData();
        this.dim = volumeData.getDimension();
    }

    public void GetXSlice(int slice, WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int z = 0; z < height; z++) {
            for (int y = 0; y < width; y++) {
                float val = grey[z][y][slice];
                image_writer.setColor(y, z, Color.color(val, val, val));
            }
        }
    }

    public void GetXMIP(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        float currentMax;
        PixelWriter image_writer = image.getPixelWriter();

        for (int z = 0; z < height; z++) {
            for (int y = 0; y < width; y++) {
                currentMax = 0f;
                for (int x = 0; x < dim; x++) {
                    currentMax = Math.max(currentMax, grey[z][y][x]);
                }
                image_writer.setColor(y, z, Color.color(currentMax, currentMax, currentMax));
            }
        }
    }

    public void GetXVol(WritableImage image, double skinOpacity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double a_accum, r_accum, g_accum, b_accum;
        double sigma, sampleR, sampleG, sampleB;
        short[][][] ct = volumeData.getCTData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Initialise the accumulators for front-to-back compositing:
                a_accum = 1.0;
                r_accum = 0.0;
                g_accum = 0.0;
                b_accum = 0.0;

                // Cast the ray along the z-axis:
                for (int z = 0; z < dim; z++) {
                    short ctVal = ct[y][x][z];
                    // Apply transfer function:
                    if (ctVal < -300) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else if (ctVal >= -300 && ctVal <= 49) {
                        // Use slider-modulated opacity: at slider=0 skinOpacity=0 (transparent), at slider=100 skinOpacity=1 (fully opaque)
                        sigma = skinOpacity;
                        sampleR = 0.82;
                        sampleG = 0.49;
                        sampleB = 0.18;
                    } else if (ctVal >= 50 && ctVal <= 299) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else { // ctVal >= 300 (bone)
                        sigma = 0.8;
                        sampleR = sampleG = sampleB = 1.0;
                    }

                    // Front-to-back compositing:
                    r_accum += a_accum * sigma * sampleR;
                    g_accum += a_accum * sigma * sampleG;
                    b_accum += a_accum * sigma * sampleB;
                    a_accum *= (1 - sigma);

                    // Early ray termination:
                    if (a_accum < 0.03) {
                        break;
                    }
                }

                // Set the final pixel color; output fully opaque.
                image_writer.setColor(x, y, new Color(clamp(r_accum), clamp(g_accum), clamp(b_accum), 1.0));
            }
        }
    }

    // Y Slice: plane at y = slice; image coordinates (x,y)
    public void GetYSlice(int slice, WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                float val = grey[z][slice][x];
                image_writer.setColor(x, z, Color.color(val, val, val));
            }
        }
    }

    public void GetYMIP(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        float currentMax;
        PixelWriter image_writer = image.getPixelWriter();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                currentMax = 0f;
                for (int y = 0; y < dim; y++) {
                    currentMax = Math.max(currentMax, grey[z][y][x]);
                }
                image_writer.setColor(x, z, Color.color(currentMax, currentMax, currentMax));
            }
        }
    }

    public void GetYVol(WritableImage image, double skinOpacity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double a_accum, r_accum, g_accum, b_accum;
        double sigma, sampleR, sampleG, sampleB;
        short[][][] ct = volumeData.getCTData();

        // Loop over each output pixel (x,y)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Initialise the accumulators for front-to-back compositing:
                a_accum = 1.0;
                r_accum = 0.0;
                g_accum = 0.0;
                b_accum = 0.0;

                // Cast the ray along the z-axis:
                for (int z = 0; z < dim; z++) {
                    short ctVal = ct[y][z][x];
                    // Apply transfer function:
                    if (ctVal < -300) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else if (ctVal >= -300 && ctVal <= 49) {
                        // Use slider-modulated opacity: at slider=0 skinOpacity=0 (transparent), at slider=100 skinOpacity=1 (fully opaque)
                        sigma = skinOpacity;
                        sampleR = 0.82;
                        sampleG = 0.49;
                        sampleB = 0.18;
                    } else if (ctVal >= 50 && ctVal <= 299) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else { // ctVal >= 300 (bone)
                        sigma = 0.8;
                        sampleR = sampleG = sampleB = 1.0;
                    }

                    // Front-to-back compositing:
                    r_accum += a_accum * sigma * sampleR;
                    g_accum += a_accum * sigma * sampleG;
                    b_accum += a_accum * sigma * sampleB;
                    a_accum *= (1 - sigma);

                    // Early ray termination:
                    if (a_accum < 0.03) {
                        break;
                    }
                }

                // Set the final pixel color; output fully opaque.
                image_writer.setColor(x, y, new Color(clamp(r_accum), clamp(g_accum), clamp(b_accum), 1.0));
            }
        }
    }

    public void GetZSlice(int slice, WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float val = grey[slice][y][x];
                image_writer.setColor(x, y, Color.color(val, val, val));
            }
        }
    }

    public void GetZMIP(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        float currentMax;
        PixelWriter image_writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                currentMax = 0f;
                for (int z = dim - 1; z >= 0; z--) {
                    currentMax = Math.max(currentMax, grey[z][y][x]);
                }
                image_writer.setColor(x, y, Color.color(currentMax, currentMax, currentMax));
            }
        }
    }

    public void GetZVol(WritableImage image, double skinOpacity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double a_accum, r_accum, g_accum, b_accum;
        double sigma, sampleR, sampleG, sampleB;
        short[][][] ct = volumeData.getCTData();

        // Loop over each output pixel (x,y)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Initialise the accumulators for front-to-back compositing:
                a_accum = 1.0;
                r_accum = 0.0;
                g_accum = 0.0;
                b_accum = 0.0;

                // Cast the ray along the z-axis:
                for (int z = 0; z < dim; z++) {
                    short ctVal = ct[z][y][x];
                    // Apply transfer function:
                    if (ctVal < -300) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else if (ctVal >= -300 && ctVal <= 49) {
                        // Use slider-modulated opacity: at slider=0 skinOpacity=0 (transparent), at slider=100 skinOpacity=1 (fully opaque)
                        sigma = skinOpacity;
                        sampleR = 0.82;
                        sampleG = 0.49;
                        sampleB = 0.18;
                    } else if (ctVal >= 50 && ctVal <= 299) {
                        sigma = 0.0;
                        sampleR = sampleG = sampleB = 0.0;
                    } else { // ctVal >= 300 (bone)
                        sigma = 0.8;
                        sampleR = sampleG = sampleB = 1.0;
                    }

                    // Front-to-back compositing:
                    r_accum += a_accum * sigma * sampleR;
                    g_accum += a_accum * sigma * sampleG;
                    b_accum += a_accum * sigma * sampleB;
                    a_accum *= (1 - sigma);

                    // Early ray termination:
                    if (a_accum < 0.03) {
                        break;
                    }
                }

                // Set the final pixel color; output fully opaque.
                image_writer.setColor(x, y, new Color(clamp(r_accum), clamp(g_accum), clamp(b_accum), 1.0));
            }
        }
    }

    private double clamp(double value) {
        return Math.min(1.0, Math.max(0.0, value));
    }
}
