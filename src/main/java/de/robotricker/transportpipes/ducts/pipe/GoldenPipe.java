package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.TPDirection;

public class GoldenPipe extends Pipe {

    public GoldenPipe(BlockLoc blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BasicDuctType.valueOf("Pipe").ductTypeValueOf("Golden"));
    }

    public enum Color {
        BLUE("Blue", TPDirection.EAST),
        YELLOW("Yellow", TPDirection.WEST),
        RED("Red", TPDirection.SOUTH),
        WHITE("White", TPDirection.NORTH),
        GREEN("Green", TPDirection.UP),
        BLACK("Black", TPDirection.DOWN);

        private String displayName;
        private TPDirection direction;

        Color(String displayName, TPDirection direction) {
            this.displayName = displayName;
            this.direction = direction;
        }

        public String getDisplayName() {
            return displayName;
        }

        public TPDirection getDirection() {
            return direction;
        }

        public static Color getByDir(TPDirection dir){
            for (Color c : values()) {
                if (c.direction.equals(dir)) {
                    return c;
                }
            }
            return null;
        }
    }

}
