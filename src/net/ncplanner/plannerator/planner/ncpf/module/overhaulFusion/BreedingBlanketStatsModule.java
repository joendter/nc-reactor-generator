package net.ncplanner.plannerator.planner.ncpf.module.overhaulFusion;
import net.ncplanner.plannerator.ncpf.io.NCPFObject;
import net.ncplanner.plannerator.ncpf.module.NCPFModule;
import net.ncplanner.plannerator.planner.ncpf.module.NCPFRecipeStatsModule;
public class BreedingBlanketStatsModule extends NCPFRecipeStatsModule{
    public boolean augmented;
    public float efficiency;
    public float heat;
    public BreedingBlanketStatsModule(){
        super("plannerator:fusion_test:breeding_blanket_stats");
    }
    @Override
    public void convertFromObject(NCPFObject ncpf){
        augmented = ncpf.getBoolean("augmented");
        efficiency = ncpf.getFloat("efficiency");
        heat = ncpf.getFloat("heat");
    }
    @Override
    public void convertToObject(NCPFObject ncpf){
        ncpf.setBoolean("augmented", augmented);
        ncpf.setFloat("efficiency", efficiency);
        ncpf.setFloat("heat", heat);
    }
    @Override
    public String getTooltip(){
        String ttp = "";
        ttp+="Efficiency: "+efficiency+"\n";
        ttp+="Heat: "+heat+"\n";
        if(augmented)ttp+="Augmented"+"\n";
        return ttp;
    }
    @Override
    public void conglomerate(NCPFModule addon){
        BreedingBlanketStatsModule stats = (BreedingBlanketStatsModule)addon;
        augmented = stats.augmented;
        efficiency = stats.efficiency;
        heat = stats.heat;
    }
}