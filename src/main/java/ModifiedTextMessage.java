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
import burp.api.montoya.core.Annotations;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

public class ModifiedTextMessage implements InterceptedTextMessage {
    String payload;
    Direction direction;
    Annotations annotations;

    public ModifiedTextMessage() {}

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String payload() {
        return this.payload;
    }

    @Override
    public Direction direction() {
        return this.direction;
    }

    @Override
    public Annotations annotations() {
        return this.annotations;
    }
}
