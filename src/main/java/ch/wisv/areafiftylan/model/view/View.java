package ch.wisv.areafiftylan.model.view;

/**
 * This class is responsible for hiding information in the JSON responses from Controllers. It's just an empty class
 * with a few interfaces which define the level of visibility. In your model, if you mark a field with
 *
 * @JSONView([view]), only those fields will be displayed if you ALSO mark the returning Controller method with the same
 * annotation.
 * <p>
 * View interfaces can be extended. Controllers returning this view will then also return their parent view. Use this to
 * create visibilty levels.
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

    public interface OrderOverview extends NoProfile {}
}
