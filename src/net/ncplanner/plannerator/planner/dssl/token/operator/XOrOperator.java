package net.ncplanner.plannerator.planner.dssl.token.operator;
import net.ncplanner.plannerator.planner.dssl.Script;
import net.ncplanner.plannerator.planner.dssl.object.StackBool;
import net.ncplanner.plannerator.planner.dssl.object.StackObject;
public class XOrOperator extends Operator{
    public XOrOperator(){
        super("^");
    }
    @Override
    public Operator newInstance(){
        return new XOrOperator();
    }
    @Override
    public StackObject evaluate(Script script, StackObject v1, StackObject v2){
        return new StackBool(v1.asBool().getValue()^v2.asBool().getValue());
    }
}