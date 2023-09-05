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
package socketsleuth.intruder.payloads.models;

import javax.swing.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 * StringPayloadModel is a concrete implementation of IPayloadModel for
 * a basic list of String payloads.
 */
public class StringPayloadModel implements IPayloadModel<String> {

    private final DefaultListModel<String> listModel;

    public StringPayloadModel() {
        this.listModel = new DefaultListModel<>();
    }

    /**
     * Adds a new String payload to the model.
     *
     * @param payload the String payload to add
     */
    public void addPayload(String payload) {
        listModel.addElement(payload);
    }

    /**
     * Removes a String payload from the model by its index.
     *
     * @param index the index of the String payload to remove
     */
    public void removePayload(int index) {
        listModel.remove(index);
    }

    /**
     * Removes duplicate String payloads from the model.
     */
    public void removeDuplicates() {
        HashSet<String> seen = new HashSet<>();
        for (int i = listModel.size() - 1; i >= 0; i--) {
            String payload = listModel.get(i);
            if (seen.contains(payload)) {
                listModel.remove(i);
            } else {
                seen.add(payload);
            }
        }
    }

    /**
     * Returns an iterator to iterate through the String payloads.
     *
     * @return an Iterator<String> instance
     */
    @Override
    public Iterator<String> iterator() {
        return listModel.elements().asIterator();
    }

    /**
     * Returns the underlying list model used to store the list of string payloads.
     *
     * @return a DefaultListModel<String> instance
     */
    public DefaultListModel<String> getListModel() {
        return this.listModel;
    }
}
