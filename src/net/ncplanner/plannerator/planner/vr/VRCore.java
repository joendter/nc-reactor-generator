package net.ncplanner.plannerator.planner.vr;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.graphics.image.Color;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.MathUtil;
import net.ncplanner.plannerator.planner.gui.Menu;
import net.ncplanner.plannerator.planner.gui.menu.MenuMain;
import net.ncplanner.plannerator.planner.vr.menu.VRMenuMain;
import org.joml.Matrix4f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.TrackedDevicePose;
import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRCompositor.*;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VRSystem;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetEyeToHeadTransform;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetProjectionMatrix;
import static org.lwjgl.openvr.VRSystem.VRSystem_PollNextEvent;
public class VRCore{
    public static ArrayList<Long> VRFPStracker = new ArrayList<>();
    public static int leftEyeBuffer = -1;
    public static int rightEyeBuffer = -1;
    public static final int vrWidth = 2016;//*15/32;
    public static final int vrHeight = 2240;//*15/32;
    private static ArrayList<ArrayList<Integer>> pressedButtons = new ArrayList<>();
    private static ArrayList<ArrayList<Integer>> touchedButtons = new ArrayList<>();
    public static VRGUI vrgui = new VRGUI();
    private static boolean running = true;
    public static Multitool leftMultitool = new Multitool();
    public static Multitool rightMultitool = new Multitool();
    public static void start(){
        glfwSwapInterval(0);
        IntBuffer peError = IntBuffer.allocate(1);
        int token = VR_InitInternal(peError, EVRApplicationType_VRApplication_Scene);
        if(peError.get(0)!=0){
            throw new RuntimeException("Failed to initialize VR!\nINIT ERROR SYMBOL: " + VR_GetVRInitErrorAsSymbol(peError.get(0))+"\nINIT ERROR  DESCR: " + VR_GetVRInitErrorAsEnglishDescription(peError.get(0)));
        }
        OpenVR.create(token);
        Core.gui.open(new Menu(Core.gui, Core.gui.menu){
            @Override
            public void onClosed(){
                running = false;
                OpenVR.destroy();
                VR_ShutdownInternal();
                glfwSwapInterval(1);
            }
            @Override
            public void render3d(double deltaTime){
                super.render3d(deltaTime);
                Renderer renderer = new Renderer();
                renderer.unbindTexture();
                renderer.setWhite();
                if(leftEyeBuffer==-1){
                    leftEyeBuffer = createFramebuffer(vrWidth, vrHeight);
                }
                if(rightEyeBuffer==-1){
                    rightEyeBuffer = leftEyeBuffer;
//                    rightEyeBuffer = createFramebuffer(vrWidth, vrHeight);
                }
                Color background = Core.theme.getMenuBackgroundColor();
                TrackedDevicePose.Buffer tdpb = TrackedDevicePose.create(k_unMaxTrackedDeviceCount);
                TrackedDevicePose.Buffer tdpb2 = TrackedDevicePose.create(k_unMaxTrackedDeviceCount);
                VRCompositor_WaitGetPoses(tdpb, tdpb2);
                //<editor-fold defaultstate="collapsed" desc="Process VREvents">
                VREvent event;
                while(running&&VRSystem_PollNextEvent(event = VREvent.malloc())){
                    int type = event.eventType();
                    System.out.println("VR Event type="+type);
                    if(type==EVREventType_VREvent_None)System.out.println("- None");
                    if(type==EVREventType_VREvent_TrackedDeviceActivated)System.out.println("- TrackedDeviceActivated");
                    if(type==EVREventType_VREvent_TrackedDeviceDeactivated)System.out.println("- TrackedDeviceDeactivated");
                    if(type==EVREventType_VREvent_TrackedDeviceUpdated)System.out.println("- TrackedDeviceUpdated");
                    if(type==EVREventType_VREvent_TrackedDeviceUserInteractionStarted)System.out.println("- TrackedDeviceUserInteractionStarted");
                    if(type==EVREventType_VREvent_TrackedDeviceUserInteractionEnded)System.out.println("- TrackedDeviceUserInteractionEnded");
                    if(type==EVREventType_VREvent_IpdChanged)System.out.println("- IpdChanged");
                    if(type==EVREventType_VREvent_EnterStandbyMode)System.out.println("- EnterStandbyMode");
                    if(type==EVREventType_VREvent_LeaveStandbyMode)System.out.println("- LeaveStandbyMode");
                    if(type==EVREventType_VREvent_TrackedDeviceRoleChanged)System.out.println("- TrackedDeviceRoleChanged");
                    if(type==EVREventType_VREvent_WatchdogWakeUpRequested)System.out.println("- WatchdogWakeUpRequested");
                    if(type==EVREventType_VREvent_LensDistortionChanged)System.out.println("- LensDistortionChanged");
                    if(type==EVREventType_VREvent_PropertyChanged)System.out.println("- PropertyChanged");
                    if(type==EVREventType_VREvent_WirelessDisconnect)System.out.println("- WirelessDisconnect");
                    if(type==EVREventType_VREvent_WirelessReconnect)System.out.println("- WirelessReconnect");
                    if(type==EVREventType_VREvent_ButtonPress){
                        System.out.println("- ButtonPress "+event.trackedDeviceIndex()+" "+event.data().controller().button());
                        while(event.trackedDeviceIndex()>=pressedButtons.size())pressedButtons.add(new ArrayList<>());
                        pressedButtons.get(event.trackedDeviceIndex()).add(event.data().controller().button());
                        vrgui.onKeyEvent(event.trackedDeviceIndex(), event.data().controller().button(), true);
                    }
                    if(type==EVREventType_VREvent_ButtonUnpress){
                        System.out.println("- ButtonUnpress "+event.trackedDeviceIndex()+" "+event.data().controller().button());
                        while(event.trackedDeviceIndex()>=pressedButtons.size())pressedButtons.add(new ArrayList<>());
                        pressedButtons.get(event.trackedDeviceIndex()).remove((Integer)event.data().controller().button());
                        vrgui.onKeyEvent(event.trackedDeviceIndex(), event.data().controller().button(), false);
                    }
                    if(type==EVREventType_VREvent_ButtonTouch){
                        System.out.println("- ButtonTouch "+event.trackedDeviceIndex()+" "+event.data().controller().button());
                        while(event.trackedDeviceIndex()>=touchedButtons.size())touchedButtons.add(new ArrayList<>());
                        touchedButtons.get(event.trackedDeviceIndex()).add(event.data().controller().button());
                    }
                    if(type==EVREventType_VREvent_ButtonUntouch){
                        System.out.println("- ButtonUntouch "+event.trackedDeviceIndex()+" "+event.data().controller().button());
                        while(event.trackedDeviceIndex()>=touchedButtons.size())touchedButtons.add(new ArrayList<>());
                        touchedButtons.get(event.trackedDeviceIndex()).remove((Integer)event.data().controller().button());
                    }
                    if(type==EVREventType_VREvent_DualAnalog_Press)System.out.println("- DualAnalog_Press");
                    if(type==EVREventType_VREvent_DualAnalog_Unpress)System.out.println("- DualAnalog_Unpress");
                    if(type==EVREventType_VREvent_DualAnalog_Touch)System.out.println("- DualAnalog_Touch");
                    if(type==EVREventType_VREvent_DualAnalog_Untouch)System.out.println("- DualAnalog_Untouch");
                    if(type==EVREventType_VREvent_DualAnalog_Move)System.out.println("- DualAnalog_Move");
                    if(type==EVREventType_VREvent_DualAnalog_ModeSwitch1)System.out.println("- DualAnalog_ModeSwitch1");
                    if(type==EVREventType_VREvent_DualAnalog_ModeSwitch2)System.out.println("- DualAnalog_ModeSwitch2");
                    if(type==EVREventType_VREvent_DualAnalog_Cancel)System.out.println("- DualAnalog_Cancel");
                    if(type==EVREventType_VREvent_MouseMove)System.out.println("- MouseMove");
                    if(type==EVREventType_VREvent_MouseButtonDown)System.out.println("- MouseButtonDown");
                    if(type==EVREventType_VREvent_MouseButtonUp)System.out.println("- MouseButtonUp");
                    if(type==EVREventType_VREvent_FocusEnter)System.out.println("- FocusEnter");
                    if(type==EVREventType_VREvent_FocusLeave)System.out.println("- FocusLeave");
                    if(type==EVREventType_VREvent_ScrollDiscrete)System.out.println("- ScrollDiscrete");
                    if(type==EVREventType_VREvent_TouchPadMove)System.out.println("- TouchPadMove");
                    if(type==EVREventType_VREvent_OverlayFocusChanged)System.out.println("- OverlayFocusChanged");
                    if(type==EVREventType_VREvent_ReloadOverlays)System.out.println("- ReloadOverlays");
                    if(type==EVREventType_VREvent_ScrollSmooth)System.out.println("- ScrollSmooth");
                    if(type==EVREventType_VREvent_InputFocusCaptured)System.out.println("- InputFocusCaptured");
                    if(type==EVREventType_VREvent_InputFocusReleased)System.out.println("- InputFocusReleased");
                    if(type==EVREventType_VREvent_SceneFocusLost)System.out.println("- SceneFocusLost");
                    if(type==EVREventType_VREvent_SceneFocusGained)System.out.println("- SceneFocusGained");
                    if(type==EVREventType_VREvent_SceneApplicationChanged)System.out.println("- SceneApplicationChanged");
                    if(type==EVREventType_VREvent_SceneFocusChanged)System.out.println("- SceneFocusChanged");
                    if(type==EVREventType_VREvent_InputFocusChanged)System.out.println("- InputFocusChanged");
                    if(type==EVREventType_VREvent_SceneApplicationSecondaryRenderingStarted)System.out.println("- SceneApplicationSecondaryRenderingStarted");
                    if(type==EVREventType_VREvent_SceneApplicationUsingWrongGraphicsAdapter)System.out.println("- SceneApplicationUsingWrongGraphicsAdapter");
                    if(type==EVREventType_VREvent_ActionBindingReloaded)System.out.println("- ActionBindingReloaded");
                    if(type==EVREventType_VREvent_HideRenderModels)System.out.println("- HideRenderModels");
                    if(type==EVREventType_VREvent_ShowRenderModels)System.out.println("- ShowRenderModels");
                    if(type==EVREventType_VREvent_ConsoleOpened)System.out.println("- ConsoleOpened");
                    if(type==EVREventType_VREvent_ConsoleClosed)System.out.println("- ConsoleClosed");
                    if(type==EVREventType_VREvent_OverlayShown)System.out.println("- OverlayShown");
                    if(type==EVREventType_VREvent_OverlayHidden)System.out.println("- OverlayHidden");
                    if(type==EVREventType_VREvent_DashboardActivated)System.out.println("- DashboardActivated");
                    if(type==EVREventType_VREvent_DashboardDeactivated)System.out.println("- DashboardDeactivated");
                    if(type==EVREventType_VREvent_DashboardRequested)System.out.println("- DashboardRequested");
                    if(type==EVREventType_VREvent_ResetDashboard)System.out.println("- ResetDashboard");
                    if(type==EVREventType_VREvent_RenderToast)System.out.println("- RenderToast");
                    if(type==EVREventType_VREvent_ImageLoaded)System.out.println("- ImageLoaded");
                    if(type==EVREventType_VREvent_ShowKeyboard)System.out.println("- ShowKeyboard");
                    if(type==EVREventType_VREvent_HideKeyboard)System.out.println("- HideKeyboard");
                    if(type==EVREventType_VREvent_OverlayGamepadFocusGained)System.out.println("- OverlayGamepadFocusGained");
                    if(type==EVREventType_VREvent_OverlayGamepadFocusLost)System.out.println("- OverlayGamepadFocusLost");
                    if(type==EVREventType_VREvent_OverlaySharedTextureChanged)System.out.println("- OverlaySharedTextureChanged");
                    if(type==EVREventType_VREvent_ScreenshotTriggered)System.out.println("- ScreenshotTriggered");
                    if(type==EVREventType_VREvent_ImageFailed)System.out.println("- ImageFailed");
                    if(type==EVREventType_VREvent_DashboardOverlayCreated)System.out.println("- DashboardOverlayCreated");
                    if(type==EVREventType_VREvent_SwitchGamepadFocus)System.out.println("- SwitchGamepadFocus");
                    if(type==EVREventType_VREvent_RequestScreenshot)System.out.println("- RequestScreenshot");
                    if(type==EVREventType_VREvent_ScreenshotTaken)System.out.println("- ScreenshotTaken");
                    if(type==EVREventType_VREvent_ScreenshotFailed)System.out.println("- ScreenshotFailed");
                    if(type==EVREventType_VREvent_SubmitScreenshotToDashboard)System.out.println("- SubmitScreenshotToDashboard");
                    if(type==EVREventType_VREvent_ScreenshotProgressToDashboard)System.out.println("- ScreenshotProgressToDashboard");
                    if(type==EVREventType_VREvent_PrimaryDashboardDeviceChanged)System.out.println("- PrimaryDashboardDeviceChanged");
                    if(type==EVREventType_VREvent_RoomViewShown)System.out.println("- RoomViewShown");
                    if(type==EVREventType_VREvent_RoomViewHidden)System.out.println("- RoomViewHidden");
                    if(type==EVREventType_VREvent_ShowUI)System.out.println("- ShowUI");
                    if(type==EVREventType_VREvent_ShowDevTools)System.out.println("- ShowDevTools");
                    if(type==EVREventType_VREvent_Notification_Shown)System.out.println("- Notification_Shown");
                    if(type==EVREventType_VREvent_Notification_Hidden)System.out.println("- Notification_Hidden");
                    if(type==EVREventType_VREvent_Notification_BeginInteraction)System.out.println("- Notification_BeginInteraction");
                    if(type==EVREventType_VREvent_Notification_Destroyed)System.out.println("- Notification_Destroyed");
                    if(type==EVREventType_VREvent_Quit){
                        System.out.println("- Quit");
                        gui.open(new MenuMain(gui));
                    }
                    if(type==EVREventType_VREvent_ProcessQuit)System.out.println("- ProcessQuit");
                    if(type==EVREventType_VREvent_QuitAborted_UserPrompt)System.out.println("- QuitAborted_UserPrompt");
                    if(type==EVREventType_VREvent_QuitAcknowledged)System.out.println("- QuitAcknowledged");
                    if(type==EVREventType_VREvent_DriverRequestedQuit)System.out.println("- DriverRequestedQuit");
                    if(type==EVREventType_VREvent_RestartRequested)System.out.println("- RestartRequested");
                    if(type==EVREventType_VREvent_ChaperoneDataHasChanged)System.out.println("- ChaperoneDataHasChanged");
                    if(type==EVREventType_VREvent_ChaperoneUniverseHasChanged)System.out.println("- ChaperoneUniverseHasChanged");
                    if(type==EVREventType_VREvent_ChaperoneTempDataHasChanged)System.out.println("- ChaperoneTempDataHasChanged");
                    if(type==EVREventType_VREvent_ChaperoneSettingsHaveChanged)System.out.println("- ChaperoneSettingsHaveChanged");
                    if(type==EVREventType_VREvent_SeatedZeroPoseReset)System.out.println("- SeatedZeroPoseReset");
                    if(type==EVREventType_VREvent_ChaperoneFlushCache)System.out.println("- ChaperoneFlushCache");
                    if(type==EVREventType_VREvent_ChaperoneRoomSetupStarting)System.out.println("- ChaperoneRoomSetupStarting");
                    if(type==EVREventType_VREvent_ChaperoneRoomSetupFinished)System.out.println("- ChaperoneRoomSetupFinished");
                    if(type==EVREventType_VREvent_AudioSettingsHaveChanged)System.out.println("- AudioSettingsHaveChanged");
                    if(type==EVREventType_VREvent_BackgroundSettingHasChanged)System.out.println("- BackgroundSettingHasChanged");
                    if(type==EVREventType_VREvent_CameraSettingsHaveChanged)System.out.println("- CameraSettingsHaveChanged");
                    if(type==EVREventType_VREvent_ReprojectionSettingHasChanged)System.out.println("- ReprojectionSettingHasChanged");
                    if(type==EVREventType_VREvent_ModelSkinSettingsHaveChanged)System.out.println("- ModelSkinSettingsHaveChanged");
                    if(type==EVREventType_VREvent_EnvironmentSettingsHaveChanged)System.out.println("- EnvironmentSettingsHaveChanged");
                    if(type==EVREventType_VREvent_PowerSettingsHaveChanged)System.out.println("- PowerSettingsHaveChanged");
                    if(type==EVREventType_VREvent_EnableHomeAppSettingsHaveChanged)System.out.println("- EnableHomeAppSettingsHaveChanged");
                    if(type==EVREventType_VREvent_SteamVRSectionSettingChanged)System.out.println("- SteamVRSectionSettingChanged");
                    if(type==EVREventType_VREvent_LighthouseSectionSettingChanged)System.out.println("- LighthouseSectionSettingChanged");
                    if(type==EVREventType_VREvent_NullSectionSettingChanged)System.out.println("- NullSectionSettingChanged");
                    if(type==EVREventType_VREvent_UserInterfaceSectionSettingChanged)System.out.println("- UserInterfaceSectionSettingChanged");
                    if(type==EVREventType_VREvent_NotificationsSectionSettingChanged)System.out.println("- NotificationsSectionSettingChanged");
                    if(type==EVREventType_VREvent_KeyboardSectionSettingChanged)System.out.println("- KeyboardSectionSettingChanged");
                    if(type==EVREventType_VREvent_PerfSectionSettingChanged)System.out.println("- PerfSectionSettingChanged");
                    if(type==EVREventType_VREvent_DashboardSectionSettingChanged)System.out.println("- DashboardSectionSettingChanged");
                    if(type==EVREventType_VREvent_WebInterfaceSectionSettingChanged)System.out.println("- WebInterfaceSectionSettingChanged");
                    if(type==EVREventType_VREvent_TrackersSectionSettingChanged)System.out.println("- TrackersSectionSettingChanged");
                    if(type==EVREventType_VREvent_LastKnownSectionSettingChanged)System.out.println("- LastKnownSectionSettingChanged");
                    if(type==EVREventType_VREvent_DismissedWarningsSectionSettingChanged)System.out.println("- DismissedWarningsSectionSettingChanged");
                    if(type==EVREventType_VREvent_StatusUpdate)System.out.println("- StatusUpdate");
                    if(type==EVREventType_VREvent_WebInterface_InstallDriverCompleted)System.out.println("- WebInterface_InstallDriverCompleted");
                    if(type==EVREventType_VREvent_MCImageUpdated)System.out.println("- MCImageUpdated");
                    if(type==EVREventType_VREvent_FirmwareUpdateStarted)System.out.println("- FirmwareUpdateStarted");
                    if(type==EVREventType_VREvent_FirmwareUpdateFinished)System.out.println("- FirmwareUpdateFinished");
                    if(type==EVREventType_VREvent_KeyboardClosed)System.out.println("- KeyboardClosed");
                    if(type==EVREventType_VREvent_KeyboardCharInput)System.out.println("- KeyboardCharInput");
                    if(type==EVREventType_VREvent_KeyboardDone)System.out.println("- KeyboardDone");
                    if(type==EVREventType_VREvent_ApplicationTransitionStarted)System.out.println("- ApplicationTransitionStarted");
                    if(type==EVREventType_VREvent_ApplicationTransitionAborted)System.out.println("- ApplicationTransitionAborted");
                    if(type==EVREventType_VREvent_ApplicationTransitionNewAppStarted)System.out.println("- ApplicationTransitionNewAppStarted");
                    if(type==EVREventType_VREvent_ApplicationListUpdated)System.out.println("- ApplicationListUpdated");
                    if(type==EVREventType_VREvent_ApplicationMimeTypeLoad)System.out.println("- ApplicationMimeTypeLoad");
                    if(type==EVREventType_VREvent_ApplicationTransitionNewAppLaunchComplete)System.out.println("- ApplicationTransitionNewAppLaunchComplete");
                    if(type==EVREventType_VREvent_ProcessConnected)System.out.println("- ProcessConnected");
                    if(type==EVREventType_VREvent_ProcessDisconnected)System.out.println("- ProcessDisconnected");
                    if(type==EVREventType_VREvent_Compositor_MirrorWindowShown)System.out.println("- Compositor_MirrorWindowShown");
                    if(type==EVREventType_VREvent_Compositor_MirrorWindowHidden)System.out.println("- Compositor_MirrorWindowHidden");
                    if(type==EVREventType_VREvent_Compositor_ChaperoneBoundsShown)System.out.println("- Compositor_ChaperoneBoundsShown");
                    if(type==EVREventType_VREvent_Compositor_ChaperoneBoundsHidden)System.out.println("- Compositor_ChaperoneBoundsHidden");
                    if(type==EVREventType_VREvent_Compositor_DisplayDisconnected)System.out.println("- Compositor_DisplayDisconnected");
                    if(type==EVREventType_VREvent_Compositor_DisplayReconnected)System.out.println("- Compositor_DisplayReconnected");
                    if(type==EVREventType_VREvent_Compositor_HDCPError)System.out.println("- Compositor_HDCPError");
                    if(type==EVREventType_VREvent_Compositor_ApplicationNotResponding)System.out.println("- Compositor_ApplicationNotResponding");
                    if(type==EVREventType_VREvent_Compositor_ApplicationResumed)System.out.println("- Compositor_ApplicationResumed");
                    if(type==EVREventType_VREvent_Compositor_OutOfVideoMemory)System.out.println("- Compositor_OutOfVideoMemory");
                    if(type==EVREventType_VREvent_TrackedCamera_StartVideoStream)System.out.println("- TrackedCamera_StartVideoStream");
                    if(type==EVREventType_VREvent_TrackedCamera_StopVideoStream)System.out.println("- TrackedCamera_StopVideoStream");
                    if(type==EVREventType_VREvent_TrackedCamera_PauseVideoStream)System.out.println("- TrackedCamera_PauseVideoStream");
                    if(type==EVREventType_VREvent_TrackedCamera_ResumeVideoStream)System.out.println("- TrackedCamera_ResumeVideoStream");
                    if(type==EVREventType_VREvent_TrackedCamera_EditingSurface)System.out.println("- TrackedCamera_EditingSurface");
                    if(type==EVREventType_VREvent_PerformanceTest_EnableCapture)System.out.println("- PerformanceTest_EnableCapture");
                    if(type==EVREventType_VREvent_PerformanceTest_DisableCapture)System.out.println("- PerformanceTest_DisableCapture");
                    if(type==EVREventType_VREvent_PerformanceTest_FidelityLevel)System.out.println("- PerformanceTest_FidelityLevel");
                    if(type==EVREventType_VREvent_MessageOverlay_Closed)System.out.println("- MessageOverlay_Closed");
                    if(type==EVREventType_VREvent_MessageOverlayCloseRequested)System.out.println("- MessageOverlayCloseRequested");
                    if(type==EVREventType_VREvent_Input_HapticVibration)System.out.println("- Input_HapticVibration");
                    if(type==EVREventType_VREvent_Input_BindingLoadFailed)System.out.println("- Input_BindingLoadFailed");
                    if(type==EVREventType_VREvent_Input_BindingLoadSuccessful)System.out.println("- Input_BindingLoadSuccessful");
                    if(type==EVREventType_VREvent_Input_ActionManifestReloaded)System.out.println("- Input_ActionManifestReloaded");
                    if(type==EVREventType_VREvent_Input_ActionManifestLoadFailed)System.out.println("- Input_ActionManifestLoadFailed");
                    if(type==EVREventType_VREvent_Input_ProgressUpdate)System.out.println("- Input_ProgressUpdate");
                    if(type==EVREventType_VREvent_Input_TrackerActivated)System.out.println("- Input_TrackerActivated");
                    if(type==EVREventType_VREvent_Input_BindingsUpdated)System.out.println("- Input_BindingsUpdated");
                    if(type==EVREventType_VREvent_SpatialAnchors_PoseUpdated)System.out.println("- SpatialAnchors_PoseUpdated");
                    if(type==EVREventType_VREvent_SpatialAnchors_DescriptorUpdated)System.out.println("- SpatialAnchors_DescriptorUpdated");
                    if(type==EVREventType_VREvent_SpatialAnchors_RequestPoseUpdate)System.out.println("- SpatialAnchors_RequestPoseUpdate");
                    if(type==EVREventType_VREvent_SpatialAnchors_RequestDescriptorUpdate)System.out.println("- SpatialAnchors_RequestDescriptorUpdate");
                    if(type==EVREventType_VREvent_SystemReport_Started)System.out.println("- SystemReport_Started");
                    if(type==EVREventType_VREvent_VendorSpecific_Reserved_Start)System.out.println("- VendorSpecific_Reserved_Start");
                    if(type==EVREventType_VREvent_VendorSpecific_Reserved_End)System.out.println("- VendorSpecific_Reserved_End");
                }
    //</editor-fold>
                Matrix4f[] projectionMatrices = new Matrix4f[2];
                for(int i = 0; i<2; i++){
                    projectionMatrices[i] = MathUtil.convertHmdMatrix(VRSystem_GetProjectionMatrix(i, .01f, 1000, HmdMatrix44.calloc())).transpose();//near and far
                }
                Matrix4f[] eyeMatrices = new Matrix4f[2];
                TrackedDevicePose hmdPose = tdpb.get(k_unTrackedDeviceIndex_Hmd);
                Matrix4f headPose = new Matrix4f();
                if(hmdPose.bDeviceIsConnected()&&hmdPose.bPoseIsValid()){
                    headPose = new Matrix4f(MathUtil.convertHmdMatrix(hmdPose.mDeviceToAbsoluteTracking())).invert();
                }
                for(int i = 0; i<2; i++){
                    eyeMatrices[i] = new Matrix4f(MathUtil.convertHmdMatrix(VRSystem_GetEyeToHeadTransform(i, HmdMatrix34.calloc()))).invert().mul(headPose);
                }
                glBindBuffer(GL_FRAMEBUFFER, leftEyeBuffer);
                glViewport(0, 0, vrWidth, vrHeight);
                glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
                renderer.projection(projectionMatrices[0]);
                renderer.view(eyeMatrices[0]);
                VRCore.render(renderer, tdpb, deltaTime);
                glBindBuffer(GL_FRAMEBUFFER, 0);
                renderer.setWhite();
                Texture textureLeft = Texture.create();
                textureLeft.set(fboTextures.get(leftEyeBuffer), ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Auto);
                int left = VRCompositor_Submit(EVREye_Eye_Left, textureLeft, null, EVRSubmitFlags_Submit_Default);
                //<editor-fold defaultstate="collapsed" desc="Left Eye Error">
                switch(left){
                    case EVRCompositorError_VRCompositorError_None:
                        break;
                    case EVRCompositorError_VRCompositorError_RequestFailed:
                        System.out.println("LRequestFailed");
                        break;
                    case EVRCompositorError_VRCompositorError_IncompatibleVersion:
                        System.out.println("LIncompatibleVersion");
                        break;
                    case EVRCompositorError_VRCompositorError_DoNotHaveFocus:
                        System.out.println("LDoNotHaveFocus");
                        break;
                    case EVRCompositorError_VRCompositorError_InvalidTexture:
                        System.out.println("LInvalidTexture");
                        break;
                    case EVRCompositorError_VRCompositorError_IsNotSceneApplication:
                        System.out.println("LIsNotSceneApplication");
                        break;
                    case EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice:
                        System.out.println("LTextureIsOnWrongDevice");
                        break;
                    case EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat:
                        System.out.println("LTextureUsesUnsupportedFormat");
                        break;
                    case EVRCompositorError_VRCompositorError_SharedTexturesNotSupported:
                        System.out.println("LSharedTexturesNotSupported");
                        break;
                    case EVRCompositorError_VRCompositorError_IndexOutOfRange:
                        System.out.println("LIndexOutOfRange");
                        break;
                    case EVRCompositorError_VRCompositorError_AlreadySubmitted:
                        System.out.println("LAlreadySubmitted");
                        break;
                    case EVRCompositorError_VRCompositorError_InvalidBounds:
                        System.out.println("LInvalidBounds");
                        break;
                }
    //</editor-fold>
                glBindBuffer(GL_FRAMEBUFFER, rightEyeBuffer);
                glViewport(0, 0, vrWidth, vrHeight);
                glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
                renderer.projection(projectionMatrices[1]);
                renderer.view(eyeMatrices[1]);
                VRCore.render(renderer, tdpb2, deltaTime);
                glBindBuffer(GL_FRAMEBUFFER, 0);
                renderer.setWhite();
                Texture textureRight = Texture.create();
                textureRight.set(fboTextures.get(rightEyeBuffer), ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Auto);
                int right = VRCompositor_Submit(EVREye_Eye_Right, textureRight, null, EVRSubmitFlags_Submit_Default);
                //<editor-fold defaultstate="collapsed" desc="Right Eye Error">
                switch(right){
                    case EVRCompositorError_VRCompositorError_None:
                        break;
                    case EVRCompositorError_VRCompositorError_RequestFailed:
                        System.out.println("RRequestFailed");
                        break;
                    case EVRCompositorError_VRCompositorError_IncompatibleVersion:
                        System.out.println("RIncompatibleVersion");
                        break;
                    case EVRCompositorError_VRCompositorError_DoNotHaveFocus:
                        System.out.println("RDoNotHaveFocus");
                        break;
                    case EVRCompositorError_VRCompositorError_InvalidTexture:
                        System.out.println("RInvalidTexture");
                        break;
                    case EVRCompositorError_VRCompositorError_IsNotSceneApplication:
                        System.out.println("RIsNotSceneApplication");
                        break;
                    case EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice:
                        System.out.println("RTextureIsOnWrongDevice");
                        break;
                    case EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat:
                        System.out.println("RTextureUsesUnsupportedFormat");
                        break;
                    case EVRCompositorError_VRCompositorError_SharedTexturesNotSupported:
                        System.out.println("RSharedTexturesNotSupported");
                        break;
                    case EVRCompositorError_VRCompositorError_IndexOutOfRange:
                        System.out.println("RIndexOutOfRange");
                        break;
                    case EVRCompositorError_VRCompositorError_AlreadySubmitted:
                        System.out.println("RAlreadySubmitted");
                        break;
                    case EVRCompositorError_VRCompositorError_InvalidBounds:
                        System.out.println("RInvalidBounds");
                        break;
                }
    //</editor-fold>
                VRFPStracker.add(System.currentTimeMillis());
                while(VRFPStracker.get(0)<System.currentTimeMillis()-5_000){
                    VRFPStracker.remove(0);
                }
                glViewport(0, 0, Core.gui.getWidth(), Core.gui.getHeight());
                renderer.projection(new Matrix4f().setPerspective(45, Core.gui.getWidth()/(float)Core.gui.getHeight(), 0.1f, 100));
                renderer.setWhite();
            }
            @Override
            public void render2d(double deltaTime){
                super.render2d(deltaTime);
                Renderer renderer = new Renderer();
                renderer.drawTexture(fboTextures.get(leftEyeBuffer), 0, 0, gui.getWidth(), gui.getHeight());
            }
        });
        vrgui.open(new VRMenuMain(vrgui));
    }
    public static long getVRFPS(){
        return VRFPStracker.size()/5;
    }
    public static void render(Renderer renderer, TrackedDevicePose.Buffer tdpb, double deltaTime){
        vrgui.render(renderer, tdpb, deltaTime);
        renderer.setWhite();
        //<editor-fold defaultstate="collapsed" desc="Tracked Devices">
        glDisable(GL_CULL_FACE);
        for(int i = 1; i<tdpb.limit(); i++){
            TrackedDevicePose pose = tdpb.get(i);
            if(pose.bDeviceIsConnected()&&pose.bPoseIsValid()){
                IntBuffer pError = IntBuffer.allocate(1);
                int role = VRSystem.VRSystem_GetInt32TrackedDeviceProperty(i, ETrackedDeviceProperty_Prop_ControllerRoleHint_Int32, pError);
                if(role==ETrackedControllerRole_TrackedControllerRole_LeftHand||role==ETrackedControllerRole_TrackedControllerRole_RightHand){
                    Matrix4f matrix = new Matrix4f(MathUtil.convertHmdMatrix(pose.mDeviceToAbsoluteTracking()));
                    if(role==ETrackedControllerRole_TrackedControllerRole_LeftHand){
                        renderer.pushModel(matrix.scale(-1, 1, 1));
                        leftMultitool.device = i;
                        leftMultitool.render(renderer);
                    }
                    if(role==ETrackedControllerRole_TrackedControllerRole_RightHand){
                        renderer.pushModel(matrix);
                        rightMultitool.device = i;
                        rightMultitool.render(renderer);
                    }
                    renderer.popModel();
                }
            }
        }
        glEnable(GL_CULL_FACE);
//</editor-fold>
    }
    private static final HashMap<Integer, Integer> fboRBO = new HashMap<>();
    private static final HashMap<Integer, Integer> fboTextures = new HashMap<>();
    public static int createFramebuffer(int width, int height){
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
        fboRBO.put(framebuffer, rbo);
        fboTextures.put(framebuffer, textureColorBuffer);
        return framebuffer;
    }
    public static void destroyFramebuffer(int fbo){
        glDeleteFramebuffers(fbo);
        glDeleteBuffers(fboRBO.remove(fbo));
        glDeleteTextures(fboTextures.remove(fbo));
    }
}