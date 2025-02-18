package net.ncplanner.plannerator.planner.ncpf.configuration.overhaulMSR;
import java.util.function.Supplier;
import net.ncplanner.plannerator.multiblock.configuration.IBlockRecipe;
import net.ncplanner.plannerator.ncpf.NCPFElement;
import net.ncplanner.plannerator.ncpf.element.NCPFElementDefinition;
import net.ncplanner.plannerator.ncpf.io.NCPFObject;
import net.ncplanner.plannerator.ncpf.module.NCPFModule;
import net.ncplanner.plannerator.planner.ncpf.module.DisplayNameModule;
import net.ncplanner.plannerator.planner.ncpf.module.TextureModule;
import net.ncplanner.plannerator.planner.ncpf.module.overhaulMSR.HeaterStatsModule;
public class HeaterRecipe extends NCPFElement implements IBlockRecipe{
    public DisplayNameModule names = new DisplayNameModule();
    public TextureModule texture = new TextureModule();
    public HeaterStatsModule stats = new HeaterStatsModule();
    public HeaterRecipe(){}
    public HeaterRecipe(NCPFElementDefinition definition){
        super(definition);
    }
    @Override
    public void convertFromObject(NCPFObject ncpf){
        super.convertFromObject(ncpf);
        stats = getModule(HeaterStatsModule::new);
        names = getModule(DisplayNameModule::new);
        texture = getModule(TextureModule::new);
    }
    @Override
    public void convertToObject(NCPFObject ncpf){
        setModules(stats, names, texture);
        super.convertToObject(ncpf);
    }
    @Override
    public String getTitle(){
        return "Heater Recipe";
    }
    @Override
    public Supplier<NCPFModule>[] getPreferredModules(){
        return new Supplier[]{HeaterStatsModule::new};
    }
}