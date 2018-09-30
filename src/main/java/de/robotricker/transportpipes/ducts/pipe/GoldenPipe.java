package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.DuctService;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class GoldenPipe extends Pipe {

    public GoldenPipe(DuctService ductService, BlockLocation blockLoc, World world, Chunk chunk) {
        super(ductService, blockLoc, world, chunk, BaseDuctType.valueOf("Pipe").ductTypeValueOf("Golden"));
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
