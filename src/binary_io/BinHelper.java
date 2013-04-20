package binary_io;

public class BinHelper {

    public static String addLeadingZeros(String str, int length) {
        int nbMissingZeros = length - str.length();
        for (int i = 0; i < nbMissingZeros; i++) {
            str = "0" + str;
        }
        return str;
    }
    
}
