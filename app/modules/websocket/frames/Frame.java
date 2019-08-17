package modules.websocket.frames;

/**
 * Generic frame that specifies that all frames must have a type
 */
public interface Frame {
     /**
      * The type of the frame
     */
     String getType();
}
