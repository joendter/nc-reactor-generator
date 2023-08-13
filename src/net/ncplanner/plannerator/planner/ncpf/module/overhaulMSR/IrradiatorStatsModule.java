package net.ncplanner.plannerator.planner.ncpf.module.overhaulMSR;
import net.ncplanner.plannerator.ncpf.io.NCPFObject;
import net.ncplanner.plannerator.ncpf.module.NCPFModule;
import net.ncplanner.plannerator.planner.ncpf.module.NCPFRecipeStatsModule;
public class IrradiatorStatsModule extends NCPFRecipeStatsModule{
    public float efficiency;
    public float heat;
    public IrradiatorStatsModule(){
        super("nuclearcraft:overhaul_msr:irradiator_stats");
    }
    @Override
    public void convertFromObject(NCPFObject ncpf){
        efficiency = ncpf.getFloat("efficiency");
        heat = ncpf.getFloat("heat");
    }
    @Override
    public void convertToObject(NCPFObject ncpf){
        ncpf.setFloat("efficiency", efficiency);
        ncpf.setFloat("heat", heat);
    }
    @Override
    public String getTooltip(){
        return "Efficiency: "+efficiency+"\n"
             + "Heat: "+heat+"\n";
    }
    @Override
    public void conglomerate(NCPFModule addon){
        IrradiatorStatsModule stats = (IrradiatorStatsModule)addon;
        efficiency = stats.efficiency;
        heat = stats.heat;
    }
}