package net.ncplanner.plannerator.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
public class ConfigList extends ConfigBase{
    private ArrayList<ConfigBase> lst = new ArrayList<>();
    public ConfigList(){}
    public ConfigList(Collection<?> lst){
        for(Object o : lst){
            add(o);
        }
    }
    @Override
    void read(DataInputStream in, short version) throws IOException{
        int oneType = version==0?2:in.read();//Version 0 is before the One Type system was implemented- disable it.
        if(oneType==0) return;//Empty list
        if(oneType==1){
            int count = in.readInt();
            int index = in.read();
            ConfigBase base;
            for(int i=0; i<count; i++){
                base = newConfig(index);
                base.read(in, version);
                lst.add(base);
            }
        }else{
            int index;
            ConfigBase base;
            while((index = in.read())>0){
                base = newConfig(index);
                if(version==0) in.readShort();//This is a UTF-8 empty string, 0x0000, the "name" of the objects in the list.  Removed as extraneous in Version 1.
                base.read(in, version);
                lst.add(base);
            }
        }
    }
    @Override
    void write(DataOutputStream out) throws IOException {
        if(lst.isEmpty()){
            out.write(0);
            return;
        }
        boolean oneType = lst.size()>4;
        if(oneType){
            ConfigBase ref = lst.get(0);
            for(ConfigBase b : lst){
                if(b.getClass()!=ref.getClass()){
                    oneType = false;
                    break;
                }
            }
        }
        out.write(oneType?1:2);
        if(oneType){
            out.writeInt(lst.size());
            out.write(lst.get(0).getIndex());
            for(ConfigBase b : lst){
                b.write(out);
            }
        }else{
            for(ConfigBase base : lst){
                out.write(base.getIndex());
                base.write(out);
            }
            out.write(0);
        }
    }
    @Override
    ConfigList getData() {
        return this;
    }
    public <V> V get(int index){
        if(index>=size()||index<0) return null;
        else return (V)lst.get(index).getData();
    }
    public Config getConfig(int index){
        return get(index);
    }
    public String getString(int index){
        return get(index);
    }
    public int getInt(int index){
        return get(index);
    }
    public boolean getBoolean(int index){
        return get(index);
    }
    public float getFloat(int index){
        return get(index);
    }
    public long getLong(int index){
        return get(index);
    }
    public double getDouble(int index){
        return get(index);
    }
    public byte getByte(int index){
        return get(index);
    }
    public short getShort(int index){
        return get(index);
    }
    public Number getNumber(int index){
        return get(index);
    }
    public int getAsInt(int index){
        return getNumber(index).intValue();
    }
    public float getAsFloat(int index){
        return getNumber(index).floatValue();
    }
    public long getAsLong(int index){
        return getNumber(index).longValue();
    }
    public double getAsDouble(int index){
        return getNumber(index).doubleValue();
    }
    public byte getAsByte(int index){
        return getNumber(index).byteValue();
    }
    public short getAsShort(int index){
        return getNumber(index).shortValue();
    }
    public ConfigNumberList getConfigNumberList(int index){
        return get(index);
    }
    public ConfigList getConfigList(int index){
        return get(index);
    }
    public <V> Collection<V> copyTo(Collection<V> lst){
        for(V v : this.<V>iterable()){
            lst.add(v);
        }
        return lst;
    }
    public <V> Iterable<V> iterable(){
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return ConfigList.this.iterator();
            }
        };
    }
    public <V> Iterator<V> iterator(){
        return new Iterator<V>(){
            Iterator<ConfigBase> it = lst.iterator();
            V next;
            @Override
            public boolean hasNext() {
                while(next==null&&it.hasNext()){
                    try{
                        next = (V)it.next().getData();
                    }catch(ClassCastException ex){}
                }
                return next!=null;
            }
            @Override
            public V next() {
                hasNext();
                V val = next;
                next = null;
                return val;
            }
        };
    }
    public int size(){
        return lst.size();
    }
    public <V> V remove(int index){
        V val = get(index);
        if(index>=0&&index<size()) lst.remove(index);
        return val;
    }
    public boolean contains(Object value){
        if(value==null) return false;//Block null
        for(ConfigBase b : lst){
            if(b.getData().equals(value)) return true;
        }
        return false;
    }
    public void add(Config value){
        doAdd(value);
    }
    public void add(String value){
        doAdd(new ConfigString(value));
    }
    public void add(int value){
        doAdd(new ConfigInteger(value));
    }
    public void add(boolean value){
        doAdd(new ConfigBoolean(value));
    }
    public void add(float value){
        doAdd(new ConfigFloat(value));
    }
    public void add(long value){
        doAdd(new ConfigLong(value));
    }
    public void add(double value){
        doAdd(new ConfigDouble(value));
    }
    public void add(byte value){
        doAdd(new ConfigByte(value));
    }
    public void add(short value){
        doAdd(new ConfigShort(value));
    }
    public void add(ConfigList value){
        doAdd(value);
    }
    public void add(ConfigNumberList value){
        doAdd(value);
    }
    public void add(Object value){
        if(value==null){
        }else if(value instanceof Config){
            add((Config)value);
        }else if(value instanceof String){
            add((String)value);
        }else if(value instanceof Integer){
            add((int)value);
        }else if(value instanceof Boolean){
            add((boolean)value);
        }else if(value instanceof Float){
            add((float)value);
        }else if(value instanceof Long){
            add((long)value);
        }else if(value instanceof Double){
            add((double)value);
        }else if(value instanceof Byte){
            add((byte)value);
        }else if(value instanceof Short){
            add((short)value);
        }else if(value instanceof ConfigList){
            add((ConfigList)value);
        }else if(value instanceof ConfigNumberList){
            add((ConfigNumberList)value);
        }
    }
    private void doAdd(ConfigBase b){
        if(b==null) throw new IllegalArgumentException("Cannot set null values to a config!");
        lst.add(b);
    }
    public void add(int idx, Config value){
        doAdd(idx, value);
    }
    public void add(int idx, String value){
        doAdd(idx, new ConfigString(value));
    }
    public void add(int idx, int value){
        doAdd(idx, new ConfigInteger(value));
    }
    public void add(int idx, boolean value){
        doAdd(idx, new ConfigBoolean(value));
    }
    public void add(int idx, float value){
        doAdd(idx, new ConfigFloat(value));
    }
    public void add(int idx, long value){
        doAdd(idx, new ConfigLong(value));
    }
    public void add(int idx, double value){
        doAdd(idx, new ConfigDouble(value));
    }
    public void add(int idx, byte value){
        doAdd(idx, new ConfigByte(value));
    }
    public void add(int idx, short value){
        doAdd(idx, new ConfigShort(value));
    }
    public void add(int idx, ConfigList value){
        doAdd(idx, value);
    }
    public void add(int idx, ConfigNumberList value){
        doAdd(idx, value);
    }
    public void add(int idx, Object value){
        if(value==null){
        }else if(value instanceof Config){
            add(idx, (Config)value);
        }else if(value instanceof String){
            add(idx, (String)value);
        }else if(value instanceof Integer){
            add(idx, (int)value);
        }else if(value instanceof Boolean){
            add(idx, (boolean)value);
        }else if(value instanceof Float){
            add(idx, (float)value);
        }else if(value instanceof Long){
            add(idx, (long)value);
        }else if(value instanceof Double){
            add(idx, (double)value);
        }else if(value instanceof Byte){
            add(idx, (byte)value);
        }else if(value instanceof Short){
            add(idx, (short)value);
        }else if(value instanceof ConfigList){
            add(idx, (ConfigList)value);
        }else if(value instanceof ConfigNumberList){
            add(idx, (ConfigNumberList)value);
        }
    }
    private void doAdd(int idx, ConfigBase b){
        if(b==null) throw new IllegalArgumentException("Cannot set null values to a config!");
        lst.add(idx, b);
    }
}
