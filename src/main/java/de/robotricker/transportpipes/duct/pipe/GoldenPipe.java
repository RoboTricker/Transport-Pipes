package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.Direction;

public class GoldenPipe extends Pipe {

    public GoldenPipe(BlockLocation blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BaseDuctType.valueOf("Pipe").ductTypeValueOf("Golden"));
    }

    public enum Color {
        BLUE("Blue", Direction.EAST),
        YELLOW("Yellow", Direction.WEST),
        RED("Red", Direction.SOUTH),
        WHITE("White", Direction.NORTH),
        GREEN("Green", Direction.UP),
        BLACK("Black", Direction.DOWN);

        private String displayName;
        private Direction direction;

        Color(String displayName, Direction direction) {
            this.displayName = displayName;
            this.direction = direction;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Direction getDirection() {
            return direction;
        }

        public static Color getByDir(Direction dir){
            for (Color c : values()) {
                if (c.direction.equals(dir)) {
                    return c;
                }
            }
            return null;
        }
    }

}
