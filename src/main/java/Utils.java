import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {

    // Ugly but it works - detects \x01\x02\x03 hex strings
    public static boolean isHexString(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i < input.length() - 3 && input.charAt(i + 1) == 'x' && Character.digit(input.charAt(i + 2), 16) != -1 && Character.digit(input.charAt(i + 3), 16) != -1) {
                i += 4;
            } else {
                return false;
            }
        }

        return true;
    }

    public static byte[] hexStringToByteArray(String input) {
        if (!isHexString(input)) {
            return new byte[0];
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < input.length(); i += 4) {
            if (input.charAt(i) == '\\' && input.charAt(i + 1) == 'x') {
                int b = (Character.digit(input.charAt(i + 2), 16) << 4) + Character.digit(input.charAt(i + 3), 16);
                byteArrayOutputStream.write(b);
            }
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static boolean byteArrayContains(byte[] container, byte[] target) {
        for (int i = 0; i <= container.length - target.length; i++) {
            boolean found = true;
            for (int j = 0; j < target.length; j++) {
                if (container[i + j] != target[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    public static byte[] replace(byte[] input, byte[] find, byte[] replaceWith) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int inputLength = input.length;
        int findLength = find.length;
        int replaceWithLength = replaceWith.length;
        int i = 0;
        while (i < inputLength) {
            if (i <= inputLength - findLength && Arrays.equals(Arrays.copyOfRange(input, i, i + findLength), find)) {
                output.write(replaceWith, 0, replaceWithLength);
                i += findLength;
            } else {
                output.write(input[i]);
                i++;
            }
        }
        return output.toByteArray();
    }

    public static boolean isRegex(String input) {
        try {
            Pattern.compile(input);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public static String replace(String input, String find, String replaceWith) {
        if (isRegex(find)) {
            return input.replaceAll(find, replaceWith);
        } else {
            return input.replace(find, replaceWith);
        }
    }
}
