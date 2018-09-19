package de.robotricker.transportpipes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.factory.PipeFactory;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.ColoredPipeType;
import de.robotricker.transportpipes.duct.types.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.pipe.modelled.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.VanillaPipeRenderSystem;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.Direction;
import de.robotricker.transportpipes.util.ItemUtils;

import javax.inject.Inject;

public class DuctManager {

    private final ProtocolService ductProtocol;

    private final Map<World, Map<BlockLocation, Duct>> ducts;
    private final List<RenderSystem> renderSystems;
    private final Map<Player, Set<Duct>> ductsForPlayers;

    @Inject
    DuctManager(ProtocolService protocol) {
        this.ducts = Collections.synchronizedMap(new HashMap<>());
        this.renderSystems = Collections.synchronizedList(new ArrayList<>());
        this.ductsForPlayers = Collections.synchronizedMap(new HashMap<>());
        this.ductProtocol = protocol;
    }

    public void register() {
        //basic duct registration
        BaseDuctType.registerBasicDuctType(new BaseDuctType("Pipe", new PipeFactory()));
        //pipe registration
        ItemStack item = ItemUtils.createSkullItemStack("9f38586a-2ec7-33be-a472-13939b855430", "eyJ0aW1lc3RhbXAiOjE1MDAwMzc4NDM1MTgsInByb2ZpbGVJZCI6ImE5MGI4MmIwNzE4NTQ0ZjU5YmE1MTZkMGY2Nzk2NDkwIiwicHJvZmlsZU5hbWUiOiJJbUZhdFRCSCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViYjZmNmU1NjRjM2E2MDdkZjk3OGE2ZjZmY2VkZGJkZmViOTdiOWU1YmMzZGQ4MzZkMDJiZTdjOTFlNiJ9fX0=", "blgkeU5W6OAuF3A8BSJVXaR8X2OK/YjGITjx3UTDr+Ij0qsFbXnV7AuskN2lw/KnCgqOW7xWWDaeRpRIwkXwg6IkTl2w8ZFLxvoje/GlWxuG5X2hA6/hTdfEV9rU+4hwliSnU4zABVfA0hm9uxmVjYYH0GKshPPyQnbG1DI6vBaY+qUMwvZao26qhCQeDi/HLx3X+xIxLXjmlOtAFV+pce7WJWL1VjSpRejtpyreCqc/TVanCGTTqDknJOmTKiBrUBFk6NGfPPq2sr0fwR0Aj+jdysaCujeCuSvsXoKwMEHWwTDU+GYl+Ez7bj+fPOabW1wuYzLWk7E5HlBnL74zNFBzH2GVvQFDAhpgSyMxOh4d65S1gbWgi9D03FZ+tEWdRQgGTNNnX5IVK6OCZLhwQW4YF4GbiFhst6M2YfVrJLu6j3WVWvHmBhD5OE3ytJTqTmNXWFJ46U9WOjtZFYqBqWdXBdF6Xc/Z+sRgGgUDCyN4QGchVkFp1DUt6Fq07eMvsQ6rxWeGzGq0dw7m9u56mcVyMR+JlGHNQzR76C8FEMMF/+pZG0qy1XKlNWsCLR9ePe7kURdYISbUYljSkWVhfJ5iFWfhpaquvmXW7erN6FXIc7XuhW6ZxvczQ546l5Q5Ncqzl8qnU61bdd87uxUrQHoD8G5i3iE1NmLw8FWmAaM=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("White", item, "§7"));
        item = ItemUtils.createSkullItemStack("b3ee4986-c3ad-3199-9536-13cffe591185", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgwNDM2NTEsInByb2ZpbGVJZCI6ImJiMDgwOTFhOTJlZDQzYWQ5NGYxZWRmZDc0NmIwYTJjIiwicHJvZmlsZU5hbWUiOiJJcDRuZHgiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E5YTBjNGM3MWY4ZGU2N2I2NjI2N2RhN2JjNzM4Y2FhMjFhNjg2YTIyMmRjZDFhOWFiMWRhOGYzMzI0Y2QyIn19fQ==", "rV0ZDlorreHr/AtTB3rXwJNwKYPcVmO/NWH2R4cGbfzBz/xUy87slXN/XFiMQRQMyWciaGFc6M4kK0qXg7YiuANaTgmjUVBmKhT9EK8o66J09eKayturQ4lyOOjDa6Vh9rXVix/jNcd/TOxnR5rl5nYSXIAg1qUhEFR13oijFapmrpBTH7RfHlOnx37fJkNMzbQzACFAxVENNdZRXqe4aMsOmP+pfkatFbkqKHC0h4AeDHtVJjWjYYH39NNw0knVmH9sP8O7XXwKWdXieqNYK+qvauH6Xw6BlXI/vsRxB0YFAe8tJ7xYY5aIIyUV08daEzMsb5lHju+jTzAERiHogMW6DlMIBtnm8+aWu1dkcwBxhGg/+9uebctkqsG4+gzG28OeHrj8SjkRZZ5VHsAR5elTXamAMxKz5Bb3bZVcYG2TXXgAFlJ82m2W7xeIWmWQ6wEH4MaRUbOgKBlQdCs6DnDOPryDsPvuwmROgfPXhD8+g5bdMsDghc8Pacz/2aSg+Xkgq/1dTHtFwtH/c02FQnwopi7EqHhoHyZegsP6VFa2VgAdnf64qDApFvPmXGgte4+EMPSwho+smUQE3JDv5swCqksrhSHLCoHxc+th11sP3NIbwRE2KNPfGDNjpQed82WhC+g9N7tEtv5JzqwJq+n3d9mE4/keKYCfv7VW0VE=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("Blue", item, "§1"));
        item = ItemUtils.createSkullItemStack("058a6092-456a-32c9-8e9d-5fcadf620631", "eyJ0aW1lc3RhbXAiOjE1MDAwMzc2MjY1NDUsInByb2ZpbGVJZCI6ImJkM2M2NDhiZDZhMDRmMDM5NDkzMjc1MzVjYjgzMWViIiwicHJvZmlsZU5hbWUiOiJFbW1hYVBsYXl6Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZTdkYTA2YmI0M2Q2NDcwOWVmODZmZjAzMjMyNjcyOTcwMzI3ZWZiYjFlYjNmN2Q3ZTE3MTRkMTVlODQzMzgifX19", "RJneeQtGm7DB5pH9Qbw6ynuM5KB4qSlqr+yGm2XbmQJDlvRLmHE2tI7aFrHhWGbLQ40mW2+RhGaF/te032hUydte9zgx6xXPBMLOH1DCf0U8RkyjImMK5hhLDXwg8axvo3gTMutF1qd0Lls9Fa+ZVIiTc9GxoUrXn/DnEh0Zfxam1Lkhn7M/8z58iPCEnrCN6mwO2weCbx8qTJYA1f/Z3pA/fF4Zm4+JkfHWLc927WwjyOUa3HmjcIiODQCLMVv2Gxb0Xwd8WcoOhz2cA2+HBZz0o4Ms8XIbKYN0mI9NKbYc/g3X9tEKS1U/kus+Kqyweys2lhFQBcTQ6z8wj/9WhY4np9spK+jrhvL9qk67lNFha7rxx3PqF7d2plE9EmS2jc3GWpC0Nhu+3U40DqmOCt1jjHj2luDvca5+a64LjH3FQjCSrYuYc6k0Tl11HwXv87fN3rzEnfjeoBYMWiqkhZ+iNqMfv73MemgFY2gn2vK4ZTAbJYsP8VHsIEFMamwR8TyZzmSVsSyPeLuTElSjKU66vJOpP1DeT3qY/MnXDnKAYcRTjeYEXYIWq4WSZ+1yicw5cqOTJtPHMMQik3PWmgFh4wIB3s98nuP7uE/+bcG1TtxefCf6NCGv17f9rvkAHLkJf2pIT1zZ2b5k7nRQ1dG2bBqk/1ZrevpM/ASzRqU=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("Red", item, "§4"));
        item = ItemUtils.createSkullItemStack("9bdb3363-b7e5-35ea-9fc6-02b2608ced3e", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgxMjA5ODMsInByb2ZpbGVJZCI6ImJmMzU3YmY0MDcyMTRiN2RiYTA0MjU3MWVjMzJjYTZmIiwicHJvZmlsZU5hbWUiOiJDb2xieUJlZWVlIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YTJiYjdiYzZiMWUyZjdhMTdjOWMxYWI0YjcyNzY3MjI2ZWFiNjUzZDVhZjM0OWRhZGM1NDVmMWE4NTFkMiJ9fX0=", "dXRWj5mbcy4xDYxQN3IooAMgRYBTS0VMxvrn+82iqrxHTXD4X4ihFKWr+Er5aS0jx1gC72Gk+7JDF9r7vyjVK7nitY7ThbuAmvZAulnyuOeiPpLYFiYKlQbdygHU8NIdRXw7uoW2ca0NozUAn0RDujsc6hFfsu4t3Q5G0GNhIjui4EyrCkPBmQjURbUJWOlvTLLQx1vX70Aw4DmewDMWoevwUix75HyYsom0rA1o9u0360jkiPyECLFCdm2NqY1LBCv54vC0aVQAdA3blPngLZ2iMju8ljlhzPKUdvWHJZlcavNbdvBiy9+PRmIP3eJqbq6hZjZtw8iswEWNVQh2WTle+DlG/Tv95z5C+Yyi+rIvA7qJO6Nx6bTkyWgy5yYi5HXH2W4DlrQmkq7h0vZlhy2uviGaa4dldqeDvATZNbs6EJPLCS1pubyQEvZ5MmdYMno7QOHMwHZCbSFiJ82D5PDowGGRGZuirXOiqMthS62uABSZ65zN+M9jUFxcpGvsuqVzpGsP+Yk06ZP5Oy5OuznLcjJean0Seqn0Wzo1ND6aIe7R5VTE37ogvqIqI8ARY47eXNgoXFjkm7YZg9NTO2FHoud/+e7xQWNYvNLkbm2rEbhK71WJV8g28dYLz6LMNipLnAmuCr10ZhQgGAzi8n5Qav1QS7tu13WfnBdtZLo=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("Yellow", item, "§e"));
        item = ItemUtils.createSkullItemStack("7b0b532f-d8cd-3024-bdcc-9afb25c8d790", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgwODAwMzIsInByb2ZpbGVJZCI6ImFhMDE4MTZhMTAwODRhYmRiMzQyZGYyNjBmNTZmYzJhIiwicHJvZmlsZU5hbWUiOiJEYXNfS2F0YW5hIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NjQ1NzdiNTY2MTBmZTM1NzQ5NTIwM2JhYjZmYjQyNTQyOWY0NmU0MzhkZTJiOWVmYjdjODM4YWFjMDhkZjEifX19", "d64+gwGh0B+WDlM1eqq+JIFQepKkbzkM7ZaDchUmr4iOswjbMiA96SAZxQTBVMS8A9cQR3j+u6mmsZ2iFdkY5Yb/XGG5SVUEm+JVB6CiecP5dThMxe0L7GxdsfrJBKMWi7V4TzmuluI6y5LZGnM4uy0gEelLwMZAOt6HULU0TrtNuRVkSZtgvBBXI7j3DRrVU8VEBkKcL0NEoGongnlHNZRC75qEo1PGvxiezMDJ4BkfGw9xAxGv0wMA8xqAJhiFvDwrx43TdS0nYTjV9+2HMMRfd8/7y4rCUtPpvCYdkZk9+SV3oN6rdZyJwdtyOgXUOWOgf4YHtRhaudvyrgBU7o0+LS2Zto9YroafzU2/ze2iHD02obDlmvtRHv0rr8hcwBYSH/aEB7X6zDcQpvMcp3+3PqKvirDrk1Gn16e6jrhHXdTd/5Y03fjbrP69HDhk9BIaZgOLpv4eghdNFfPuRz90JmiUuz1CqFj/YQWeHfluVAYS4rBVL6vXenWR//1b0SHL8h8KyFdXPunwKkO9gqrnZu8E7UDr5o6bl9H6nxTgvvF9lQBqOwZTXkHm17+mqwFEOtPUti9JCr6anoYyQh6F/TPdRixpx8cj6BEC/xLjetupTLGMfD/TqV3RxGiYiI4DV7vSVb8SLq24RYxg3j7CYYqmjzPQRd++RIQlP4k=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("Green", item, "§2"));
        item = ItemUtils.createSkullItemStack("7366ff1c-7c05-3c06-a582-71f15641178d", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgxOTg3MDMsInByb2ZpbGVJZCI6IjkxOGEwMjk1NTlkZDRjZTZiMTZmN2E1ZDUzZWZiNDEyIiwicHJvZmlsZU5hbWUiOiJCZWV2ZWxvcGVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMTRhMmIyMWE4MzFhMGRkOTc2MmI1ZDViOWEzMWU5MTQyOTQ1ZDE4OWNlZGQxNjA0MmVhZTk5Y2ZjZGQyZDM4In19fQ==", "Xdrdu3kHmrQAlfT22nTtAMsCJxOdH+e0JZ+M5b0NbFLZMRavMbGyuRm6cfFuOvuxQACvpbcfiW2T73IQ/S8nV/+Oe/n07QKU2AXVDvJqpV6Dc6wkYniDEcmEbaYXr3I2uhRTFQs4Q31wsvgC1i77qlizoqlDho8ynh/wlmtcCI36CRPguLXmewyAIGvUBOWRso4H/bkgwtE1OYeceHT0eI6gffca1PoRSjBCY1kAbFIkw7/wq09H3ak7MxvwfXRV/giabrblXKRmugXLHu3ayrWe+DEKx4SNre2oJZhF/PHGoMnYn7cIg9ds1Afs2eJ6l4MBcZecU/BWAEHN4h2VLMhJzPXmzZkxG9MeUqipIpEGO5kvtkQ/5noKp4Brzzv7lentb+w8hXZ3ctTRgsytEVYlehz3r/1NkT6yCIQDz3IJY9NC19x/k1483c/4o/a/B92BlMax06AkoFJJruuXJ+JwGIQI7npIxKDDLDv4mZRXA9Fd5utwnp8y2VE2Zz+PizE7aaYOlEP6JVuzVHSXuXdGOtM7Sfo9lv3jtJ16/3g7EAD0WiZKBMrFg7TmgXzx3xx4Xid0cdMMwxl4b5pSYIScnBxMnTMN+Ul+K/crsplqSjwGmuZv78TprOnGxtxM4F/9pJ+ZpyO1m5GNYK/ypGutLnrtR/Fj7Ov2CMgAdp4=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new ColoredPipeType("Black", item, "§8"));
        item = ItemUtils.createSkullItemStack("a4ad8e9d-bff3-3549-932d-f92ab895badd", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgyNjgyOTYsInByb2ZpbGVJZCI6ImZkNjBmMzZmNTg2MTRmMTJiM2NkNDdjMmQ4NTUyOTlhIiwicHJvZmlsZU5hbWUiOiJSZWFkIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mMGI4YzRjYjIyMzI5ZjkzZGQzNDExNDFkM2MxNTZkZjgyNzNjZWM1NzJmNDA2Yjc4YWI0N2NlODBjM2FjZmEifX19", "PlymD5SivmpHeBw0Dv3eJlJbOp0GmVQh0ss3dv1Ugbs3UgxDt5PlJcTXVZvdp5RmmEOpY8uyT7f5Kzfc0tKRF3qtXzq7PNXagLyRnsR4eiLQ9THchrAQJp33Jc3tLva+O1sU0fSj9nXRxorCBC/v2mxQjFI0MqN0d6OC8doGOF2+iFo6BdyIrnrgUFoYEApnRhzL8NRqPVyosKcLpFSaC8K7ooCsq+T54aeZmVcqbKsZiQobBQvrAG6iI1i8jzj3vDZB97QL3ajxGaocL+lZc4BxBIgCSlN9lPogDecWfR1T5HMMxI7W51zp3HxbIiGtRBitoGkFX4UPOO+WgjbisctFXoOydLWVOr6dV7nXWL+6nI6U/Ji1xjQ6DEWvgMBFxecCq7Zn9QDSAFxo7mJzSHP3mVLKMxpHMpw0jO/NAhpWk8N6rkt933KKdzOmKWViv8L8u95I6Wc96MOISiqK2S31coZxa4MWWsPe3GXj8L6Hor9ZFB/M4TuqcTSiOFe92ku8Vg5VcDpWiuHSpdNJSmm2did/FFsaOCNp42RQhXXYB4hsXjfxCvSIR3YIl52uJFDYC1GFPueywD5DUi0Hj8FLcNgr6EoVWsWnytSeiFTTh80NaXwcLvVKNNbgiysud51Gq8JyZ+Ee7GmsMoE3nEVZ6aCj3ng4hrd8+AUBRms=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Golden", item, "§6"));
        item = ItemUtils.createSkullItemStack("3874f281-eefe-3e26-94ab-d32a53cf3359", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgyMzQ2NTUsInByb2ZpbGVJZCI6IjE0ZDEzMThkNTAyNDQ3MDhhYWY2MWEwZWExMzM2YWI5IiwicHJvZmlsZU5hbWUiOiJGZWJydWFyMDMiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzRkYWYyODcwNDE4OTk4OWI3NzQ5YzAzNjFjYTA0MmMwMmI1ZThmMThiMjY5OGZkYjMyODBiNTYxN2RhMTE4In19fQ==", "KvPgrO0fiUs/dKmCzYim+g+trZLW/bH1dehtuytxbMrW6flswlyPMqR5XdzY46IfZTfAvVrV4GFotWyMfgaj8iBsSqo4OT0VFy7hoTwY6D7jBhYuSWYS+9ft4ZxYyJMCaRAJ69qXnJwcqRcV/vEOh12BufaJ0zBNYvZl6CfUwi/QjRDVK7wAXZqr1n5ipVy31uVcCIqpMwjkKgK3Ng4mCQefcreiu8Yc2tA9AJ7HaN7dtYpR9bK+ZLUuwb0atUaXQiieinqWdpNKaZbhzpcbVYDGu81/l0GkOWr1Q2dA8Hy9YQmThPolvkirUYCwMNtHLFnm6Ac3b88QylBZ76+VggLDhqUChsfuw632uiueAY/D1CusIgazgkUlB0EwxTYB9rX9iAPxpx660XG9AD0HYI2Div2z0aeqRlUhB3BCh8dpWrolWrJLiMtmc2gp3S9f9a+aLG3vVOm0cNT/LUqasze3PZpgqKoMS4oJyIjXQSWZj2HIbizacvan7mQ+V5u3qrclfh87f4KBJXeYZCGoWGzRDAXIFZ2Eyvsv3MwNe9aBrkFKv5u5xNAzCx3/jC1VE25tAodAsgnnUhPoFaqobt/iUD2QtJPjLbP6T8FDVZj6QDKhc2NFSOngZHo5GtwMAspWPUgl0ZnHEzd8kCoLe4U7Segp62E2C2dzYYIKovg=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Iron", item, "§7"));
        item = ItemUtils.createSkullItemStack("f5fb2bb0-b743-3401-9508-141689682ed6", "eyJ0aW1lc3RhbXAiOjE1MDAwMzgyOTg5NzYsInByb2ZpbGVJZCI6ImUzYjQ0NWM4NDdmNTQ4ZmI4YzhmYTNmMWY3ZWZiYThlIiwicHJvZmlsZU5hbWUiOiJNaW5pRGlnZ2VyVGVzdCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzOWE5NzNhNTY0OWFkOWZjOWRiZjU5MmI0MjE4YjFhMWQ2OTI4ZTVmZjQyODdmN2ZhOTUyMDRkYWEifX19", "gHFziSIaUmqFClBH3xAGqObQMEbdLMp/SicajLXdnkJjaW+2eetnvqqwsZ0vB61WRnqH6855eAfdCkz2GfUAGxDv3b2JKLBpV6Gpk2JzVEjYLn8IiE1aDdMV5KucMMlCNsays2DT/yGLRqeun97xE9dYZ3UhtiyT8HubI5cB9ppLPUXPaa4SiwhFWqEUhCzOr+EQjPrLmY8zrLv+KUMcniHRyE1Ac6c61unKwo6XE5o0lTjKyk+nr8p9nNmmb0KZUcHk5/sP3Es0OtLMXoTksFN3KmqMNDjNZOM17oJArpOPrkNJeHiLf4xW3pPGn3NNCrD+sJODHZc7OFTfpQrCinEH/oSWMAR1KuTS+OQ64N7u9JznWYpck/mZHCbVV1Jn4VIZhTIOMme0PXx6AgM08MlW/m3Kk+4udnNf+QHUy+G2W1K1/hDUAqoLcWap4e6aqmdp02wZzrL38yHCv/YKcu63mqCEhTRiOW4Ap7bmXCirVo2vpahbzSXNQy0J8Ldlq5Q/tWdGgCHiAGEFoqL6N8LWC7LEy1OViU8ovEMTwzhw4ijBaxyhCh1EsdsNEAnGm4ElAqAz4D6CgRIQ6f1nO+Q5bA5JqcM3B/kFe4ipDZElFxqrB96Pg0Qpzc+HIVkePitx2K7pa5cKErXe1ylbc3O/snIZO372SEJivUjgDB4=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Ice", item, "§b"));
        item = ItemUtils.createSkullItemStack("b9871e6c-f129-3364-ad40-b41177032a91", "eyJ0aW1lc3RhbXAiOjE1MDEwMDk2Nzk4OTUsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzExYzkzNTNlZWExM2VmM2RjZmZmNDhiZTM4MWU3YmM5MmNlMDM1NjIzMTIxYzUyM2IzOWNlODkyNzQzMjdlMzgifX19", "JrN0C/w8+56KC5bH/yYOBB/GbxcNqBRl0fqk23AmDGxUfNtzTkc79VV3PqkwfsIk/KH8yzRB/hfgDURuUNZADAttH+YqNsSrGyxU9a4XkrIxcm+CKpoMQD++2W1ftQJ6nvIMGFkp28UovWcAbIb/F8CIRPctQHsQ4qRs7QRf724/hHw6dOWrMMkcfsf4mcTdJXhSpBTq5je8i7q1kf1oPhbtyYs6Jmo0auC3XsAWcHMLPgKq5CVMeo16pPDM4HKeszXY+3OhxCua5ujCowNyBdeo4X9DiQtaSQSfturdDjwtVdagT2BK5EBJzAfQOoYFbEo/3dNN1ERZvy1E3v4BEu+4tQLXfcR3gdYLxxUwlk4dMbaTMVCEFxv+7dfeDd8ICw1sBqgwX4WkqUuSSZtaWV5OXDUyNZHMvMfE/m6cYF/rQ0Qu62DpEklVWVm624N3z26jEw3znH9uNxl7q4g/cBvOAom9V+uN2Vh07lo54o9djHJZDeCvZw/9QuhSVbQVQ3Mc0dceJAMVlgwGYKHNd1VY64lHD8jkRn1zAIa9hsiUnSHUrqlsNZIHj3AypP3fdhk511wqLW3oQO53a+i8WVyp6jVsU9sTRbXUfFnKTQMrX7/hXNNIe1X1/qkMwKjAFqAxF+9qqgt/AK1nBbvwZmUsbfxeZNn33zT/WL9V4lY=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Void", item, "§5"));
        item = ItemUtils.createSkullItemStack("e8501df6-3418-3470-afe3-723dd4a48186", "eyJ0aW1lc3RhbXAiOjE1MDEwNzYzNjQ4NDIsInByb2ZpbGVJZCI6ImJmMzU3YmY0MDcyMTRiN2RiYTA0MjU3MWVjMzJjYTZmIiwicHJvZmlsZU5hbWUiOiJDb2xieUJlZWVlIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZTdjZDdiZGMyMTkyNmMwY2RkZGVkNmJhOTExNmNkNjEzODExMTE1OWQzZjM2ZTdiZjg1ZTBlNzIxOGU5OWI3In19fQ==", "mMcIkdBMD+K13Gu7H5egtb9E70X14pobMdcGKwl5gVDX06Rriy+UIajOgKzzX5coyQsY/GxiSdk7Qj4vTrVEVbDYLtnFNCrLcWEPgB8UrG66ncZ1PpPn0T+uhGrZpLpFVhAblJ2TPiUjm5tfcVLjOF7nRroaUE29eIvksJ7fOrQN4X0qeG6bnaprV6GLUMUdeRKJHEWNGiR7ZABsj1m/NAh3Kn6hQhAp5B1cyvQti0a/xGDybrufgoRA/d7oOoch0flUM1udyB9lCdlT/mBNe1lhJj1jlLWhawLSs89MO7h01hJe0YjD0O6y4yQwb3f331EMWlVscnHADsOxEqy7sLaVaPfFJ0lT3je2bZVeKkGFWWC+ry7+B5AUMeYqh8IukL2S9AlETeu+byvpgc9lnhy8CpKmQ+5qztgOuALr0MNz+MCsj1LSEWqS8p1bYTNKIL9el/WdodTdP2j/uXtUt+h2NdoHcxUW4e1P+EED3r5Jr3+Ay9CCLcFri787jOXDVO+r8NG15J2JSOiiByIX2mLE8pX3kSaQm8jt9bQNjXeiUXprHBJ9xyui23+1BX9wsK5QFCHXSLMHPNS/6oTQlVkYQZ0ejUKBJ9I6b3KAYYmM3wwnARY2isrC/jNriFVu9CVZgJ52d5+MIPcDAbi0SYPyVSSGrJYdlTWCB3aUXLg=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Extraction", item, "§d"));
        item = ItemUtils.createSkullItemStack("2589f15a-2e13-4af3-b21c-3b1df42c98d0", "eyJ0aW1lc3RhbXAiOjE1MTY5MDQ1MTQ0NDIsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JjOWYzMWUzNzBlZWFhODliYzRlZTNjOTUzZjZkMjFiZjQzMmYyMWNlNzIyZjM4NTdlZGVmNTEyY2M4ZTMyIn19fQ==", "Tb7OI2UPy/Z6qmrdy/gwC0qt5iYYBuvSRnx06zGjACNciinz3i1gL8Cy792UtOsF2Om4h/V5ZJ4HVe8ylHFOtjjgulhYRmAm+i/6JaK+UPuYF8oVpguCS/n4rhKgqw3lHshjRckScPIM9fFCVzP3ylfQq7hpElgs/8zmnr6towf8z2hAf2e6rIy1MgPepWlD3t1usgdu9v4vIFTwneHT8vB8LQj1nbIJQucpuaTx+EDMNnFl32rRzAyTeCjBT0vKHvTxN+rOPhheOyZ14h8LatLLab9w/suSzhqgMWRCKjrznpaRFufc1VDNEjJUa4P0rMiXa1F6EqEnf+BVBJIMsijJVWtshpIUjZ5Vzle6Y6CN5aEtDxZ3xfrvwVIHlqFYtLsRJWSowPvHzTMmkoHUeMlf0AZwFvd74L4zvSb2Ux+QPWyC0lhAg3vLMyEI/1z73x6zLPjiRCY2cDeeTVuc/s5C6nwbSBAkx/2Rg+A5QQ+S87iQjg43AOGOE/FdtmKu94c7xf6vmid1e/aHoxiKfwwdRozA2sAbc1U9PgEvdYqRne/22+ahB016BmbwI6BvQPbX0BsvBbl0kv4iux1vr1GHfEn/XDLXtC1D5YiJ5mBhpvihAGDhATWs9AotbG9cFhTEUgEJzQaaMR9FRLzzhMYKR+nNW3DNWFfyLBAVxgU=");
        BaseDuctType.valueOf("Pipe").registerDuctType(new PipeType("Crafting", item, "§e"));
        //render system registration
        renderSystems.add(new ModelledPipeRenderSystem());
        renderSystems.add(new VanillaPipeRenderSystem());
    }

    public ProtocolService getDuctProtocol() {
        return ductProtocol;
    }

    public List<RenderSystem> getRenderSystems() {
        return renderSystems;
    }

    public Map<World, Map<BlockLocation, Duct>> getDucts() {
        return ducts;
    }

    public Map<BlockLocation, Duct> getDucts(World world) {
        if (ducts.containsKey(world)) {
            return ducts.get(world);
        }
        ducts.put(world, new TreeMap<>());
        return ducts.get(world);
    }

    public Duct getDuctAtLoc(World world, BlockLocation blockLoc) {
        Map<BlockLocation, Duct> ductMap = getDucts(world);
        return ductMap.get(blockLoc);
    }

    public Duct getDuctAtLoc(Location location) {
        return getDuctAtLoc(location.getWorld(), new BlockLocation(location));
    }

    public RenderSystem getRenderSystem(Player p, BaseDuctType basicDuctType) {
        synchronized (renderSystems) {
            return renderSystems.stream().filter(rs -> rs.getCurrentPlayers().contains(p) && rs.getBasicDuctType().equals(basicDuctType)).findAny().orElse(null);
        }
    }

    public List<RenderSystem> getRenderSystems(BaseDuctType basicDuctType) {
        synchronized (renderSystems) {
            return renderSystems.stream().filter(rs -> rs.getBasicDuctType().equals(basicDuctType)).collect(Collectors.toList());
        }
    }

    public void createDuct(Duct duct, List<Direction> allConnections) {
        for (RenderSystem renderSystem : getRenderSystems(duct.getDuctType().getBasicDuctType())) {
            renderSystem.createDuctASD(duct, allConnections);
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    getDuctProtocol().sendASD(p, duct.getBlockLoc(), renderSystem.getASDForDuct(duct));
                    ductsForPlayers.computeIfAbsent(p, k -> Collections.synchronizedSet(new HashSet<>())).add(duct);
                }
            }
        }
    }

    public void updateDuct(Duct duct, List<Direction> allConnections) {
        for (RenderSystem renderSystem : getRenderSystems(duct.getDuctType().getBasicDuctType())) {
            List<ArmorStandData> removeASD = new ArrayList<>();
            List<ArmorStandData> addASD = new ArrayList<>();
            renderSystem.updateDuctASD(duct, allConnections, removeASD, addASD);
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    getDuctProtocol().removeASD(p, removeASD);
                    getDuctProtocol().sendASD(p, duct.getBlockLoc(), addASD);
                }
            }
        }
    }

    public void destroyDuct(Duct duct) {
        for (RenderSystem renderSystem : getRenderSystems(duct.getDuctType().getBasicDuctType())) {
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    getDuctProtocol().removeASD(p, renderSystem.getASDForDuct(duct));
                    ductsForPlayers.computeIfAbsent(p, k -> Collections.synchronizedSet(new HashSet<>())).remove(duct);
                }
            }
            renderSystem.destroyDuctASD(duct);
        }
    }
}
