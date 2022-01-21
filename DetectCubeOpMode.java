// ACESSE https://github.com/OpenFTC/EasyOpenCV PARA TER MAIS INFORMAÇÕES SOBRE OS METODOS E FUNCÕES DO Easy OpenCV USADOS NESTE CÓDIGO

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;
import java.util.concurrent.TimeUnit;

@Autonomous
public class DetectCubeOpMode extends LinearOpMode {
    // Se estiver usando a câmera do telefone ao inves da webcam, substitua as linhas 23 e 32 pelas linha comentadas e as mencoes de "webcam" por "camera" 
    
    // Cria uma variavel que representa a webcam
    OpenCvWebcam webcam;
    //OpenCvCamera camera;
    
    @Override
    public void runOpMode() throws InterruptedException {
        
        int cameraMonitorViewId = this.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        
        // "Webcam 1" é o nome padrao da webcam. Caso tenha mudado esse nome, substitua "Webcam 1" por esse nome
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        //camera = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        
        // Ativa o processamento do video com o Pipeline que criamos
        SquareLocationDetectorOpenCV detector = new SquareLocationDetectorOpenCV(telemetry);
        webcam.setPipeline(detector);
        
        // Tempo limite para obtenção de permissão de configuração.
        webcam.setMillisecondsPermissionTimeout(2500);
        
        // Abre e configura a câmera/webcam
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                /*
                Parametros 1 e 2: Resolução da câmera    
                320 x 240
                640 x 480
                1280 x 720
                1920 x 1080
                
                Paramentro 3: Orientação
                UPRIGHT: retrato (ou webcam em orientação normal)
                SIDEWAYS_LEFT: paisagem para a esquerda
                SIDEWAYS_RIGHT; paisagem para a direita
                */
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);        
            }

            @Override
            public void onError(int errorCode)
            {
            }
        });
        
        // Inicia o autonomo
        waitForStart();
        
        // Para a detecção
        webcam.stopStreaming();
        
        while (opModeIsActive()) {
            // Pega o resultado da detecção e imprime na tela o resultado
            switch (detector.getLocation()) {
                case LEFT:
                    telemetry.addLine("LEFT");
                    break;
                case RIGHT:
                    telemetry.addLine("RIGHT");
                    break;
                case CENTER:
                    telemetry.addLine("CENTER");
                    break;    
                case NOT_FOUND:
                    telemetry.addLine("NOT_FOUND");
            }
            telemetry.update();
        }
    }
}
