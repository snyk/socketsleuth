/*
 * © 2023 Snyk Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package socketsleuth.intruder.payloads.payloads;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    public static java.util.List<String> extractPayloadPositions(String input) {
        java.util.List<String> extractedTextList = new ArrayList<>();
        Pattern pattern = Pattern.compile("§(.*?)§");
        Matcher matcher = pattern.matcher(input);

        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);

            if (start < lastEnd) {
                throw new IllegalStateException("Unclosed match found.");
            }

            lastEnd = end;
            extractedTextList.add(matcher.group(1));
        }

        return extractedTextList;
    }

    private Utils() {}
}
