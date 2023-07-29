package net.ncplanner.plannerator.planner.ncpf.module.overhaulSFR;
import net.ncplanner.plannerator.ncpf.io.NCPFObject;
import net.ncplanner.plannerator.planner.ncpf.module.BlockFunctionModule;
public class PortModule extends BlockFunctionModule{
    public boolean output;
    public PortModule(){
        super("nuclearcraft:overhaul_sfr:port");
    }
    public PortModule(boolean output){
        this();
        this.output = output;
    }
    @Override
    public void convertFromObject(NCPFObject ncpf){
        output = ncpf.getBoolean("output");
    }
    @Override
    public void convertToObject(NCPFObject ncpf){
        ncpf.setBoolean("output", output);
    }
    @Override
    public String getFunctionName(){
        return (output?"Output":"Input")+" Port";
    }
}