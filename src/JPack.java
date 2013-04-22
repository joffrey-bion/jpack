import compression.Compressor;

public class JPack {

    private static final int MODE_ERROR = 0;
    private static final int MODE_COMPRESS = 1;
    private static final int MODE_DECOMPRESS = 2;

    private static final int NB_ARGS_MIN = 2;
    private static final int NB_ARGS_MAX = 3;
    private static final int ARG_MODE = 0;
    private static final int ARG_FILE_SOURCE = 1;
    private static final int ARG_FILE_DEST = 2;

    public static void main(String[] args) {
        int mode = checkArgs(args);
        if (mode == MODE_ERROR) {
            printUsage();
            return;
        }
        String sourceName = args[ARG_FILE_SOURCE];
        String destName;
        if (args.length >= ARG_FILE_DEST)
            destName = args[ARG_FILE_DEST];
        else
            destName = sourceName + ".pck";
        if (mode == MODE_COMPRESS) {
            System.out.println("Compressing '" + sourceName + "' into '" + destName + "'");
            Compressor.compress(sourceName, destName);
        } else if (mode == MODE_DECOMPRESS) {
            System.out.println("Decompressing '" + sourceName + "' into '" + destName + "'");
            Compressor.uncompress(sourceName, destName);
        }
    }

    private static int checkArgs(String[] args) {
        if (args.length < NB_ARGS_MIN) {
            System.err.println("Too few arguments");
            return MODE_ERROR;
        } else if (args.length > NB_ARGS_MAX) {
            System.err.println("Too many arguments");
            return MODE_ERROR;
        }
        if (args[ARG_MODE].equals("-c")) {
            return MODE_COMPRESS;
        } else if (args[ARG_MODE].equals("-d")) {
            return MODE_DECOMPRESS;
        } else {
            System.err.println("Unknown mode '" + args[ARG_MODE]
                    + "', must be one of '-c' or '-d'.");
            return MODE_ERROR;
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java " + JPack.class.getSimpleName()
                + " -c|-d source [destination]");
    }
}
