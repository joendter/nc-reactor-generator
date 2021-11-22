package net.ncplanner.plannerator.planner.s_tack.token.operator;
import net.ncplanner.plannerator.planner.s_tack.object.StackFloat;
import net.ncplanner.plannerator.planner.s_tack.object.StackInt;
import net.ncplanner.plannerator.planner.s_tack.object.StackObject;
public class MinusOperator extends Operator{
    public MinusOperator(){
        super("-");
    }
    @Override
    public Operator newInstance(){
        return new MinusOperator();
    }
    @Override
    public StackObject evaluate(StackObject v1, StackObject v2){
        if(v1.getBaseType()==StackObject.Type.INT&&v2.getBaseType()==StackObject.Type.INT){
            return new StackInt(v1.asInt().getValue()-v2.asInt().getValue());
        }else{
            return new StackFloat(v1.asNumber().getValue().floatValue()-v2.asNumber().getValue().floatValue());
        }
    }
}