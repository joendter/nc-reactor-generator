package net.ncplanner.plannerator.planner.ncpf;
import java.util.HashMap;
import java.util.function.Function;
import net.ncplanner.plannerator.ncpf.NCPFDesign;
import net.ncplanner.plannerator.ncpf.NCPFFile;
import net.ncplanner.plannerator.ncpf.design.NCPFDesignDefinition;
import net.ncplanner.plannerator.ncpf.io.NCPFObject;
import net.ncplanner.plannerator.planner.ncpf.module.MetadataModule;
public class Design<T extends NCPFDesignDefinition> extends NCPFDesign<T>{
    public static HashMap<String, Function<NCPFFile, Design>> registeredDesigns = new HashMap<>();
    public MetadataModule metadata = new MetadataModule();
    public Design(NCPFFile file){
        super(file);
    }
    @Override
    public void convertFromObject(NCPFObject ncpf){
        super.convertFromObject(ncpf);
        metadata = getModule(MetadataModule::new);
    }
    @Override
    public void convertToObject(NCPFObject ncpf){
        setModule(metadata);
        super.convertToObject(ncpf);
    }
}