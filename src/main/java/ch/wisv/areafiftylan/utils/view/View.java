/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.utils.view;

/**
 * This class is responsible for hiding information in the JSON responses from Controllers. It's just an empty class
 * with a few interfaces which define the level of visibility. In your model, if you mark a field with
 * <p>
 * "@JSONView([view])", only those fields will be displayed if you ALSO mark the returning Controller method with the
 * same annotation.
 * <p>
 * View interfaces can be extended. Controllers returning this view will then also return their parent view. Use this to
 * create visibility levels.
 */
public class View {

    public interface NoProfile {}

    /**
     * The Public view only displays fields that are truly public so that no private information is shown
     */
    public interface Public {}

    /**
     * The participant view shows information only accessible by event participants, such as seating information
     */
    public interface Participants extends Public {}

    /**
     * The Team view includes username and email, next to the public fields.
     */
    public interface Team extends Public, NoProfile {}

    public interface OrderOverview extends NoProfile {}
}
