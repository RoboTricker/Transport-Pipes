package de.robotricker.transportpipes;

import javax.inject.Inject;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.ducts.types.pipetype.PipeType;
import de.robotricker.transportpipes.protocol.ProtocolService;

public class PipeManager extends DuctManager<Pipe> {

    private ProtocolService protocolService;
    private DuctRegister ductRegister;

    @Inject
    public PipeManager(ProtocolService protocolService, DuctRegister ductRegister) {
        super();
        this.protocolService = protocolService;
        this.ductRegister = ductRegister;
    }

    @Override
    public void registerDuctTypes() {
        PipeType pipeType;
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        pipeType = new ColoredPipeType(pipeBaseDuctType, "White", '7');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Blue", '1');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Red", '4');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Yellow", 'e');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Green", '2');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Black", '8');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Golden", '6');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Iron", '7');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Ice", 'b');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Void", '5');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Extraction", 'd');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Crafting", 'e');
        pipeBaseDuctType.registerDuctType(pipeType);

        //connect correctly
        pipeBaseDuctType.ductTypeOf("White").connectToAll();
        pipeBaseDuctType.ductTypeOf("Blue").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Blue");
        pipeBaseDuctType.ductTypeOf("Red").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Red");
        pipeBaseDuctType.ductTypeOf("Yellow").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Yellow");
        pipeBaseDuctType.ductTypeOf("Green").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Green");
        pipeBaseDuctType.ductTypeOf("Black").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Black");
        pipeBaseDuctType.ductTypeOf("Golden").connectToAll();
        pipeBaseDuctType.ductTypeOf("Iron").connectToAll();
        pipeBaseDuctType.ductTypeOf("Ice").connectToAll();
        pipeBaseDuctType.ductTypeOf("Void").connectToAll();
        pipeBaseDuctType.ductTypeOf("Extraction").connectToAll();
        pipeBaseDuctType.ductTypeOf("Crafting").connectToAll();
    }

    @Override
    public void tick() {

    }

    @Override
    public void createDuct(Pipe duct) {

    }

    @Override
    public void updateDuct(Pipe duct) {

    }

    @Override
    public void destroyDuct(Pipe duct) {

    }

}
