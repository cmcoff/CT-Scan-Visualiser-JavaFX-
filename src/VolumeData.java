import java.io.*;

public class VolumeData {
    public static final int DIM = 256; // Fixed for this dataset
    private short[][][] cthead;
    private float[][][] grey;
    private short min;
    private short max;

    public VolumeData(String filename) throws IOException {
        readData(filename);
    }

    //Function to read in the cthead data set
    private void readData(String filename) throws IOException {
        //If you've put the test.java in a directory called "src" and put the dataset in the parent directory, then this will be the correct path
        File file = new File(filename);
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find the equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k;
        short read;
        int b1, b2;

        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE;
        cthead = new short[DIM][DIM][DIM];
        grey = new float[DIM][DIM][DIM];

        //loop through the data reading it in
        for (k = 0; k < DIM; k++) {
            for (j = 0; j < DIM; j++) {
                for (i = 0; i < DIM; i++) {
                    b1 = ((int) in.readByte()) & 0xff;
                    b2 = ((int) in.readByte()) & 0xff;
                    read = (short) ((b2 << 8) | b1);
                    if (read < min) {
                        min = read;
                    }
                    if (read > max) {
                        max = read;
                    }
                    cthead[k][j][i] = read;
                }
            }
        }
        in.close();

        System.out.println(min+" "+max); //diagnostic - for CThead-256cubed.bin this should be -1897, 3029
        //(i.e. there are 4927 levels of grey, and now we will normalise them to 0-1 for display purposes
        //I know the min and max already, so I could have put the normalisation in the above loop, but I put it separate here
        for (k = 0; k < DIM; k++) {
            for (j = 0; j < DIM; j++) {
                for (i = 0; i < DIM; i++) {
                    grey[k][j][i] = ((float) cthead[k][j][i] - (float) min) / ((float) max - (float) min);
                }
            }
        }
    }

    public float[][][] getGreyData() {
        return grey;
    }

    public int getDimension() {
        return DIM;
    }

    public short[][][] getCTData() {
        return cthead;
    }
}
