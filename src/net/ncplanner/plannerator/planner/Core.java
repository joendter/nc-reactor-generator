package net.ncplanner.plannerator.planner;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ncplanner.plannerator.config2.Config;
import net.ncplanner.plannerator.config2.ConfigList;
import net.ncplanner.plannerator.discord.Bot;
import net.ncplanner.plannerator.graphics.Font;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.graphics.Shader;
import net.ncplanner.plannerator.graphics.image.Color;
import net.ncplanner.plannerator.graphics.image.Image;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.multiblock.configuration.Configuration;
import net.ncplanner.plannerator.multiblock.configuration.PartialConfiguration;
import net.ncplanner.plannerator.multiblock.overhaul.fissionmsr.OverhaulMSR;
import net.ncplanner.plannerator.multiblock.overhaul.fissionsfr.OverhaulSFR;
import net.ncplanner.plannerator.multiblock.overhaul.fusion.OverhaulFusionReactor;
import net.ncplanner.plannerator.multiblock.overhaul.turbine.OverhaulTurbine;
import net.ncplanner.plannerator.multiblock.underhaul.fissionsfr.UnderhaulSFR;
import net.ncplanner.plannerator.planner.file.FileFormat;
import net.ncplanner.plannerator.planner.file.FileWriter;
import net.ncplanner.plannerator.planner.file.NCPFFile;
import net.ncplanner.plannerator.planner.gui.Component;
import net.ncplanner.plannerator.planner.gui.GUI;
import net.ncplanner.plannerator.planner.gui.Menu;
import net.ncplanner.plannerator.planner.gui.menu.MenuInit;
import net.ncplanner.plannerator.planner.gui.menu.component.MulticolumnList;
import net.ncplanner.plannerator.planner.gui.menu.component.SingleColumnList;
import net.ncplanner.plannerator.planner.gui.menu.dialog.MenuCriticalError;
import net.ncplanner.plannerator.planner.gui.menu.dialog.MenuError;
import net.ncplanner.plannerator.planner.gui.menu.dialog.MenuWarningMessage;
import net.ncplanner.plannerator.planner.module.Module;
import net.ncplanner.plannerator.planner.theme.Theme;
import net.ncplanner.plannerator.planner.tutorial.Tutorial;
import net.ncplanner.plannerator.planner.vr.VRMenuComponent;
import net.ncplanner.plannerator.planner.vr.menu.component.VRMenuComponentMultiblockSettingsPanel;
import net.ncplanner.plannerator.planner.vr.menu.component.VRMenuComponentSpecialPanel;
import net.ncplanner.plannerator.planner.vr.menu.component.VRMenuComponentToolPanel;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.openvr.VR;
import static org.lwjgl.stb.STBImage.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;
public class Core{
    public static Logger logger = Logger.getLogger(Core.class.getName());
    public static GUI gui;
    public static ArrayList<Long> FPStracker = new ArrayList<>();
    public static boolean debugMode = false;
    public static final float maxYRot = 80f;
    public static float xRot = 30;
    public static float yRot = 30;
    public static final ArrayList<Multiblock> multiblocks = new ArrayList<>();
    public static final ArrayList<Multiblock> multiblockTypes = new ArrayList<>();
    public static HashMap<String, String> metadata = new HashMap<>();
    public static Configuration configuration = new Configuration(null, null, null);
    public static Theme theme = Theme.themes.get(0).get(0);
    public static boolean tutorialShown = false;
    public static Image sourceCircle = null;
    public static Image outlineSquare = null;
    public static boolean delCircle = false;
    public static int circleSize = 64;
    public static final ArrayList<Module> modules = new ArrayList<>();
    public static boolean vr = false;
    private static Callback glCallback;
    public static boolean invertUndoRedo;
    public static boolean autoBuildCasing = true;
    public static boolean recoveryMode = false;
    public static final ArrayList<String> pinnedStrs = new ArrayList<>();
    private static Random rand = new Random();
    public static String str = "";
    public static long window = 0;
    public static double lastFrame = -1;
    private static int screenWidth = 1, screenHeight = 1;
    public static Font FONT_20;
    public static Font FONT_40;
    public static Font FONT_10;
    public static Font FONT_MONO_20;
    private static boolean is3D = false;
    public static void addModule(Module m){
        modules.add(m);
    }
    public static void resetMetadata(){
        metadata.clear();
        metadata.put("Name", "");
        metadata.put("Author", "");
    }
    public static void main(String[] args) throws NoSuchMethodException{
        System.out.println("Checking for VR runtime");
        if(VR.VR_IsRuntimeInstalled()&&VR.VR_IsHmdPresent()){
            vr = true;
            System.out.println("VR runtime found!");
        }
        if(Main.isBot){
            System.out.println("Loading discord bot");
            Bot.start(args);
        }
        System.out.println("Initializing GLFW");
        if(!glfwInit())throw new RuntimeException("Failed to initialize GLFW!");
        glfwSetErrorCallback(new GLFWErrorCallbackI() {
            @Override
            public void invoke(int error, long description){
                String desc = MemoryUtil.memUTF8(description);
                System.err.println("GLFW ERROR "+error+": "+desc);//TODO proper error handling
            }
        });
        System.out.println("Initializing window");
        //window
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        //multisampling
        glfwWindowHint(GLFW_STENCIL_BITS, 4);
        glfwWindowHint(GLFW_SAMPLES, 4);
        //openGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        if(Main.headless)glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        System.out.println("Creating window");
        window = glfwCreateWindow(1200/(Main.isBot?10:1), 700/(Main.isBot?10:1), Main.applicationName+" "+VersionManager.currentVersion, 0, 0);
        if(window==0){
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window!");
        }
        GLFWImage.Buffer iconBuffer = GLFWImage.create(1);
        GLFWImage icon = GLFWImage.create();
        ByteBuffer imageData = null;
        IntBuffer iconWidth = BufferUtils.createIntBuffer(1);
        IntBuffer iconHeight = BufferUtils.createIntBuffer(1);
        try(InputStream input = getInputStream("/textures/icon.png")){
            imageData = stbi_load_from_memory(loadData(input), iconWidth, iconHeight, BufferUtils.createIntBuffer(1), 4);
        }catch(IOException ex){
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(imageData==null)throw new RuntimeException("Failed to load image: "+stbi_failure_reason());
        icon.set(iconWidth.get(0), iconHeight.get(0), imageData);
        iconBuffer.put(icon);
        glfwSetWindowIcon(window, iconBuffer);
        System.out.println("Initializing Console interface");
        Thread console = new Thread(() -> {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
                while(!glfwWindowShouldClose(window)){
                    String line = reader.readLine();
                    switch(line.trim()){
                        case "fps":
                            System.out.println("FPS: "+getFPS());
                            break;
                    }
                }
            }catch(IOException ex){}
        });
        console.setName("Console interface thread");
        console.start();

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);//THIS IS VSYNC
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            screenWidth = width;
            screenHeight = height;
            glViewport(0, 0, width, height);
        });
        GL.createCapabilities();
        
        System.out.println("Initializing render engine");
        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
        glEnable(GL_MULTISAMPLE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        if(debugMode){
            System.out.println("Creating GL Debug Callback");
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            glCallback = GLUtil.setupDebugMessageCallback();
        }
        System.out.println("Loading fonts");
        FONT_20 = Font.loadFont("standard");
        FONT_40 = Font.loadFont("high_resolution");
        FONT_10 = Font.loadFont("small");
        FONT_MONO_20 = Font.loadFont("monospaced");
        System.out.println("Initializing GUI");
        gui = new GUI(window){
            private boolean b;
            private float x,y,o,to;
            @Override
            public void processInput(double deltaTime){
                super.processInput(deltaTime);
                if(glfwGetKey(window, GLFW_KEY_C)==GLFW_PRESS&&isControlPressed()&&isShiftPressed()&&isAltPressed()){
                    throw new RuntimeException("Manually triggered debug error");//TODO might not hard-crash anymore
                }
            }
            @Override
            public void render2d(double deltaTime){
                Renderer renderer = new Renderer();
                o = o*.999f+to*.001f;
                int min = 1;
                int max = 4;
                for(int i = min; i<=max; i++){
                    renderer.setColor(1, 1, 1, ((-1/(max-min))*(i-min)+1)*o);
                    renderer.drawRegularPolygon(x-10, y, i, 10, 0);
                    renderer.drawRegularPolygon(x+10, y, i, 10, 0);
                }
                super.render2d(deltaTime);
            }
            @Override
            public int getWidth(){
                return screenWidth;
            }
            @Override
            public int getHeight(){
                return screenHeight;
            }
        };
        gui.open(new MenuInit(gui));
        System.out.println("Render initialization complete!");
        
        Shader shader = new Shader("vert.shader", "frag.shader");
        
        stbi_set_flip_vertically_on_load(true);
        Renderer renderer = new Renderer();
        Matrix4f orthoProjection = new Matrix4f().setOrtho(0, screenWidth, screenHeight, 0, 0.1f, 10f);//new Matrix4f().setPerspective(45, screenWidth/screenHeight, 0.1f, 100);
        Matrix4f perspectiveProjection = new Matrix4f().setPerspective(45, screenWidth/screenHeight, 0.1f, 100);
        while(!glfwWindowShouldClose(window)){
            Color color = theme.getMenuBackgroundColor();
            glClearColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
            double dt = 0;
            double time = glfwGetTime();
            if(lastFrame>-1){
                dt = time-lastFrame;
            }
            lastFrame = time;
            processInput(dt);
            renderer.setShader(shader);
            Matrix4f modelMatrix = new Matrix4f().setTranslation(0, 0, 0).setRotationXYZ(0, 0, 0);
            Matrix4f viewMatrix = new Matrix4f().setTranslation(0, 0, -10f);
            renderer.model(modelMatrix);
            renderer.view(viewMatrix);
            renderer.projection(perspectiveProjection);
            is3D = true;
            render3d(renderer, dt);
            //DRAW GUI
            glDisable(GL_CULL_FACE);
            renderer.projection(orthoProjection);
            is3D = false;
            render2d(renderer, dt);
            glEnable(GL_CULL_FACE);
            renderer.clearTranslationsAndBounds();
            
            FPStracker.add(System.currentTimeMillis());
            while(FPStracker.get(0)<System.currentTimeMillis()-5_000){
                FPStracker.remove(0);
            }
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        
        glfwDestroyWindow(window);
        glfwTerminate();
        
        File f = new File("settings.dat").getAbsoluteFile();
        Config settings = Config.newConfig(f);
        settings.set("theme", theme.name);
        Config modules = Config.newConfig();
        for(Module m : Core.modules){
            modules.set(m.name, m.isActive());
        }
        settings.set("modules", modules);
        settings.set("tutorialShown", tutorialShown);
        settings.set("invertUndoRedo", invertUndoRedo);
        settings.set("autoBuildCasing", autoBuildCasing);
        ConfigList pins = new ConfigList();
        for(String s : pinnedStrs)pins.add(s);
        settings.set("pins", pins);
        settings.save();
        if(debugMode)glCallback.free();
        if(Main.isBot){
            Bot.stop();
            System.exit(0);//TODO Shouldn't have to do this! :(
        }
    }
    public static void processInput(double deltaTime){
        if(glfwGetKey(window, GLFW_KEY_LEFT)==GLFW_PRESS)xRot-=deltaTime*40;
        if(glfwGetKey(window, GLFW_KEY_RIGHT)==GLFW_PRESS)xRot+=deltaTime*40;
        if(glfwGetKey(window, GLFW_KEY_UP)==GLFW_PRESS)yRot = MathUtil.min(maxYRot, MathUtil.max(-maxYRot, yRot-=deltaTime*40));
        if(glfwGetKey(window, GLFW_KEY_DOWN)==GLFW_PRESS)yRot = MathUtil.min(maxYRot, MathUtil.max(-maxYRot, yRot+=deltaTime*40));
        gui.processInput(deltaTime);
    }
    public static void render3d(Renderer renderer, double deltaTime){
        renderer.setWhite();
//        if(gui.menu instanceof MenuMain){
//            GL11.glPushMatrix();
//            GL11.glTranslated(.4, 0, -1.5);
//            GL11.glRotated(yRot, 1, 0, 0);
//            GL11.glRotated(xRot, 0, 1, 0);
//            Multiblock mb = ((MenuMain)gui.menu).getSelectedMultiblock();
//            if(mb!=null){
//                BoundingBox bbox = mb.getBoundingBox();
//                double size = MathUtil.max(bbox.getWidth(), MathUtil.max(bbox.getHeight(), bbox.getDepth()));
//                size/=mb.get3DPreviewScale();
//                GL11.glScaled(1/size, 1/size, 1/size);
//                GL11.glTranslated(-bbox.getWidth()/2d, -bbox.getHeight()/2d, -bbox.getDepth()/2d);
//                mb.draw3D();
//            }
//            GL11.glPopMatrix();
//        }
//        if(gui.menu instanceof MenuEdit){
//            MenuEdit editor = (MenuEdit)gui.menu;
//            if(editor.toggle3D.isToggledOn){
//                GL11.glMatrixMode(GL11.GL_PROJECTION);
//                GL11.glPushMatrix();
//                GL11.glLoadIdentity();
//                GL11.glOrtho(0, gui.getWidth()*gui.helper.guiScale, 0, gui.getHeight()*gui.helper.guiScale, 1f, 10000F);
//                GL11.glMatrixMode(GL11.GL_MODELVIEW);
//                GL11.glPushMatrix();
//                GL11.glTranslated(editor.toggle3D.x+editor.toggle3D.width/2, gui.getHeight()-(editor.toggle3D.y-editor.toggle3D.width/2), -1000);
////                GL11.glTranslated((double)gui.getWidth()/gui.getHeight()-.25, 0, -1);
//                GL11.glScaled(.625, .625, .625);
//                GL11.glScaled(editor.toggle3D.width, editor.toggle3D.width, editor.toggle3D.width);
//                GL11.glRotated(yRot, 1, 0, 0);
//                GL11.glRotated(xRot, 0, 1, 0);
//                Multiblock mb = editor.getMultiblock();
//                if(mb!=null){
//                    BoundingBox bbox = mb.getBoundingBox();
//                    double size = MathUtil.max(bbox.getWidth(), MathUtil.max(bbox.getHeight(), bbox.getDepth()));
//                    size/=mb.get3DPreviewScale();
//                    GL11.glScaled(1/size, 1/size, 1/size);
//                    GL11.glTranslated(-bbox.getWidth()/2d, -bbox.getHeight()/2d, -bbox.getDepth()/2d);
//                    editor.draw3D();
//                }
//                GL11.glPopMatrix();
//                GL11.glMatrixMode(GL11.GL_PROJECTION);
//                GL11.glPopMatrix();
//                GL11.glMatrixMode(GL11.GL_MODELVIEW);
//            }
//        }
//        if(gui.menu instanceof MenuCredits){
//            ((MenuCredits)gui.menu).render3D(millisSinceLastTick);
//        }
        gui.render3d(deltaTime);
    }
    public static void render2d(Renderer renderer, double deltaTime){
        renderer.setWhite();
//        if(delCircle&&sourceCircle!=null){
//            Core.deleteTexture(sourceCircle);
//            Core.deleteTexture(outlineSquare);
//            sourceCircle = outlineSquare = null;
//            delCircle = false;
//        }
//        if(sourceCircle==null){
//            sourceCircle = Core.makeImage(circleSize, circleSize, (buff) -> {
//                renderer.setColor(Color.WHITE);
//                renderer.drawCircle(buff.width/2, buff.height/2, buff.width*(4/16d), buff.width*(6/16d));
//            });
//        }
//        if(outlineSquare==null){
//            outlineSquare = Core.makeImage(32, 32, (buff) -> {
//                renderer.setColor(Color.WHITE);
//                double inset = buff.width/32d;
//                renderer.fillRect(inset, inset, buff.width-inset, inset+buff.width/16);
//                renderer.fillRect(inset, buff.width-inset-buff.width/16, buff.width-inset, buff.width-inset);
//                renderer.fillRect(inset, inset+buff.width/16, inset+buff.width/16, buff.width-inset-buff.width/16);
//                renderer.fillRect(buff.width-inset-buff.width/16, inset+buff.width/16, buff.width-inset, buff.width-inset-buff.width/16);
//            });
//        }
        gui.render2d(deltaTime);
    }
    public static long getFPS(){
        return FPStracker.size()/5;
    }
    private static final HashMap<Image, Integer> imgs = new HashMap<>();
    private static final HashMap<Image, Boolean> alphas = new HashMap<>();
    public static int getTexture(Image image){
        if(image==null)return -1;
        if(!imgs.containsKey(image)){
            imgs.put(image, loadTexture(image.getWidth(), image.getHeight(), image.getGLData()));
        }
        return imgs.get(image);
    }
    public static void deleteTexture(Image image){
        imgs.remove(image);
    }
    public static void setTheme(Theme t){
        t.onSet();
        theme = t;
        str+=t.name.charAt(0);
        if(str.length()>5)str = str.substring(1);
    }
    public static boolean isAltPressed(){
        return glfwGetKey(window, GLFW_KEY_LEFT_ALT)==GLFW_PRESS||glfwGetKey(window, GLFW_KEY_RIGHT_ALT)==GLFW_PRESS;
    }
    public static boolean isControlPressed(){
        return glfwGetKey(window, GLFW_KEY_LEFT_CONTROL)==GLFW_PRESS||glfwGetKey(window, GLFW_KEY_RIGHT_CONTROL)==GLFW_PRESS;
    }
    public static boolean isShiftPressed(){
        return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT)==GLFW_PRESS||glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT)==GLFW_PRESS;
    }
    public static Image makeImage(int width, int height, BufferRenderer r){
        boolean cull = glIsEnabled(GL_CULL_FACE);
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if(cull)glDisable(GL_CULL_FACE);
        if(depth)glDisable(GL_DEPTH_TEST);
        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(width*height*4);
        
        int framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        
        int textureColorBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureColorBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureColorBuffer, 0);
        
        int rbo = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if(status!=GL_FRAMEBUFFER_COMPLETE)throw new RuntimeException("Could not create FBO: "+status);
        
        glViewport(0, 0, width, height);
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        
        Renderer renderer = new Renderer();
        renderer.projection(new Matrix4f().setOrtho(0, width, height, 0, 0.1f, 10f));
        
        r.render(renderer, width, height);
        
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glViewport(0, 0, screenWidth, screenHeight);
        
        if(is3D)renderer.projection(new Matrix4f().setPerspective(45, screenWidth/screenHeight, 0.1f, 100));
        else renderer.projection(new Matrix4f().setOrtho(0, screenWidth, screenHeight, 0, 0.1f, 10f));
        
        glDeleteFramebuffers(framebuffer);
        glDeleteBuffers(rbo);
        glDeleteTextures(textureColorBuffer);
        
        int[] imgRGBData = new int[width*height];
        byte[] imgData = new byte[width*height*4];
        imageBuffer.rewind();
        imageBuffer.get(imgData);
        Image img = new Image(width, height);
        for(int i=0;i<imgRGBData.length;i++){
            imgRGBData[i]=(f(imgData[i*4])<<16)+(f(imgData[i*4+1])<<8)+(f(imgData[i*4+2]))+(f(imgData[i*4+3])<<24);//DO NOT Use RED, GREEN, or BLUE channel (here BLUE) for alpha data
        }
        img.setRGB(0, 0, width, height, imgRGBData, 0, width);
        if(cull)glEnable(GL_CULL_FACE);
        if(depth)glEnable(GL_DEPTH_TEST);
        return img;
    }
    public static void refreshModules(){
        multiblockTypes.clear();
        Tutorial.init();
        Configuration.clearConfigurations();
        for(Module m : modules){
            if(m.isActive()){
                m.addMultiblockTypes(multiblockTypes);
                m.addTutorials();
                m.addConfigurations();
            }
        }
    }
    public static boolean hasUnderhaulSFR(){
        for(Multiblock m : multiblockTypes){
            if(m instanceof UnderhaulSFR)return true;
        }
        return false;
    }
    public static boolean hasOverhaulSFR(){
        for(Multiblock m : multiblockTypes){
            if(m instanceof OverhaulSFR)return true;
        }
        return false;
    }
    public static boolean hasOverhaulMSR(){
        for(Multiblock m : multiblockTypes){
            if(m instanceof OverhaulMSR)return true;
        }
        return false;
    }
    public static boolean hasOverhaulTurbine(){
        for(Multiblock m : multiblockTypes){
            if(m instanceof OverhaulTurbine)return true;
        }
        return false;
    }
    public static boolean hasOverhaulFusion(){
        for(Multiblock m : multiblockTypes){
            if(m instanceof OverhaulFusionReactor)return true;
        }
        return false;
    }
    public static boolean hasAlpha(Image image){
        if(image==null)return false;
        if(!alphas.containsKey(image)){
            boolean hasAlpha = false;
            FOR:for(int x = 0; x<image.getWidth(); x++){
                for(int y = 0; y<image.getHeight(); y++){
                    if(new Color(image.getRGB(x, y)).getAlpha()!=255){
                        hasAlpha = true;
                        break FOR;
                    }
                }
            }
            alphas.put(image, hasAlpha);
        }
        return alphas.get(image);
    }
    public static int autosave(){
        File file = new File("autosave.ncpf");
        File cfgFile = new File("config_autosave.ncpf");
        int num = 1;
        while(file.exists()||cfgFile.exists()){
            file = new File("autosave"+num+".ncpf");
            cfgFile = new File("config_autosave"+num+".ncpf");
            num++;
        }
        {//multiblocks
            NCPFFile ncpf = new NCPFFile();
            ncpf.configuration = PartialConfiguration.generate(Core.configuration, Core.multiblocks);
            ncpf.multiblocks.addAll(Core.multiblocks);
            ncpf.metadata.putAll(Core.metadata);
            FileWriter.write(ncpf, file, FileWriter.NCPF);
        }
        {//configuration
            try(FileOutputStream stream = new FileOutputStream(cfgFile)){
                Config header = Config.newConfig();
                header.set("version", NCPFFile.SAVE_VERSION);
                header.set("count", 0);
                header.save(stream);
                Core.configuration.save(null, Config.newConfig()).save(stream);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
        return num;
    }
    public static boolean openURL(String link){
        Runtime rt = Runtime.getRuntime();
        try{
            switch(Main.os){
                case Main.OS_WINDOWS:
                    rt.exec("rundll32 url.dll,FileProtocolHandler "+link);
                    return true;
                case Main.OS_MACOS:
                    rt.exec("open "+link);
                    return true;
                case Main.OS_LINUX:
                    rt.exec("xdg-open "+link);
                    return true;
                default:
                    throw new RuntimeException("Failed to open webpage: Unkown OS\n"+link);
            }
        }catch(IOException ex){
            throw new RuntimeException("Failed to open webpage\n"+link, ex);
        }
    }
    public static String getCrashReportData(){
        String s = "";
        s+=Core.configuration.getCrashReportData()+"\n";
        s+="Theme: "+theme.getClass().getName()+" "+theme.name+"\n\n";
        s += "GUI menu stack:\n";
        if(gui!=null){
            Menu m = gui.menu;
            if(m==null)s+="null\n";
            while(m!=null){
                s+=m.getClass().getName()+"\n";
                if(m instanceof DebugInfoProvider){
                    s+=DebugInfoProvider.asString(1, ((DebugInfoProvider)m).getDebugInfo(new HashMap<>()));
                }
                m = m.parent;
            }
        }
        return s;
    }
    public static void setWindowTitle(String title){
        glfwSetWindowTitle(window, title);
    }
    public static void resetWindowTitle(){
        glfwSetWindowTitle(window, Main.applicationName+" "+VersionManager.currentVersion);
    }
    public static interface BufferRenderer{
        void render(Renderer renderer, int width, int height);
    }
    private static int f(byte imgData){
        return (imgData+256)&255;
    }
    public static File lastOpenFolder = new File("file").getAbsoluteFile().getParentFile();
    public static void createFileChooser(Consumer<File> onAccepted, FileFormat format) throws IOException{
        PointerBuffer path = MemoryUtil.memAllocPointer(1);
        String filter = "";
        for(String ext : format.extensions)filter+=","+ext;
        if(!filter.isEmpty())filter = filter.substring(1);
        try{
            int result = NativeFileDialog.NFD_OpenDialog(filter, lastOpenFolder.getAbsolutePath(), path);
            switch(result){
                case NativeFileDialog.NFD_OKAY:
                    String str = path.getStringUTF8();
                    File file = new File(str);
                    onAccepted.accept(file);
                    break;
                case NativeFileDialog.NFD_CANCEL:
                    break;
                default: //NFD_ERROR
                    throw new IOException(NativeFileDialog.NFD_GetError());
            }
        }finally{
            MemoryUtil.memFree(path);
        }
    }
    public static void createFileChooser(File selectedFile, Consumer<File> onAccepted, FileFormat format) throws IOException{
        PointerBuffer path = MemoryUtil.memAllocPointer(1);
        String filter = "";
        for(String ext : format.extensions)filter+=","+ext;
        if(!filter.isEmpty())filter = filter.substring(1);
        try{
            int result = NativeFileDialog.NFD_SaveDialog(filter, lastOpenFolder.getAbsolutePath(), path);
            switch(result){
                case NativeFileDialog.NFD_OKAY:
                    String str = path.getStringUTF8();
                    File file = new File(str);
                    onAccepted.accept(file);
                    break;
                case NativeFileDialog.NFD_CANCEL:
                    break;
                default: //NFD_ERROR
                    throw new IOException(NativeFileDialog.NFD_GetError());
            }
        }finally{
            MemoryUtil.memFree(path);
        }
    }
    public static boolean areImagesEqual(Image img1, Image img2) {
        if(img1==img2)return true;
        if(img1==null||img2==null)return false;
        if(img1.getWidth()!=img2.getWidth())return false;
        if(img1.getHeight()!=img2.getHeight())return false;
        for(int x = 0; x<img1.getWidth(); x++){
            for(int y = 0; y<img1.getHeight(); y++){
                if(img1.getRGB(x, y)!=img2.getRGB(x, y))return false;
            }
        }
        return true;
    }
    public static void autoSaveAndExit(){
        Throwable error = null;
        int num = 0;
        try{
            num = autosave();
        }catch(Throwable t){error = t;}
        if(error==null){
            System.out.println("Saved to autosave"+num+".ncpf");
        }else{
            System.err.println("Autosave Failed!");
        }
        Main.generateCrashReport("Manually closed on error", null);
        glfwSetWindowShouldClose(window, true);
    }
    public static int getThemeIndex(Component comp){
        if(comp.parent instanceof SingleColumnList)return comp.parent.components.indexOf(comp);
        if(comp.parent instanceof MulticolumnList)return comp.parent.components.indexOf(comp);
        return 0;
    }
    public static int getThemeIndex(VRMenuComponent comp){
        if(comp.parent instanceof VRMenuComponentSpecialPanel)return comp.parent.components.indexOf(comp);
        if(comp.parent instanceof VRMenuComponentToolPanel)return comp.parent.components.indexOf(comp);
        if(comp.parent instanceof VRMenuComponentMultiblockSettingsPanel)return comp.parent.components.indexOf(comp);
        return 0;
    }
    public static InputStream getInputStream(String path){
        try{
            if(new File("nbproject").exists()){
                return new FileInputStream(new File("src/"+path.replace("/", "/")));
            }else{
                JarFile jar = new JarFile(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ")));
                Enumeration enumEntries = jar.entries();
                while(enumEntries.hasMoreElements()){
                    JarEntry file = (JarEntry)enumEntries.nextElement();
                    if(file.getName().equals(path.replace("/", "/"))){
                        return jar.getInputStream(file);
                    }
                }
            }
            throw new IllegalArgumentException("Cannot find file: "+path);
        }catch(IOException ex){
            System.err.println("Couldn't read file: "+path);
            return null;
        }
    }
    public static ByteBuffer loadData(String path){
        return loadData(getInputStream(path));
    }
    public static ByteBuffer loadData(InputStream input){
        try(ByteArrayOutputStream output = new ByteArrayOutputStream()){
            int b;
            while((b = input.read())!=-1){
                output.write(b);
            }
            output.close();
            byte[] data = output.toByteArray();
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data);
            buffer.flip();
            return buffer;
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    private static HashMap<String, Integer> texturesCache = new HashMap<>();
    public static int loadTexture(String path){
        if(texturesCache.containsKey(path))return texturesCache.get(path);
        //read image
        ByteBuffer imageData = null;
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        try(InputStream input = getInputStream(path)){
            imageData = stbi_load_from_memory(loadData(input), width, height, BufferUtils.createIntBuffer(1), 4);
        }catch(IOException ex){
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(imageData==null)throw new RuntimeException("Failed to load image: "+stbi_failure_reason());
        //finish read image
        int texture = loadTexture(width.get(0), height.get(0), imageData);
        stbi_image_free(imageData);
        texturesCache.put(path, texture);
        return texture;
    }
    public static int loadTexture(int width, int height, ByteBuffer imageData){
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
        glGenerateMipmap(GL_TEXTURE_2D);
        return texture;
    }
    public static void warning(String message, Throwable error){
        System.err.println("Warning:");
        logger.log(Level.WARNING, message, error);
        if(Main.isBot)return;
        gui.menu = new MenuWarningMessage(gui, gui.menu, message, error);
    }
    public static void error(String message, Throwable error){
        System.err.println("Severe Error");
        logger.log(Level.SEVERE, message, error);
        if(Main.isBot)return;
        gui.menu = new MenuError(gui, gui.menu, message, error);
    }
    public static void criticalError(String message, Throwable error){
        System.err.println("Critical Error");
        logger.log(Level.SEVERE, message, error);
        if(Main.isBot)return;
        gui.menu = new MenuCriticalError(gui, message, error);
    }
}