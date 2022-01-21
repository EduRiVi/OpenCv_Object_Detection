// ACESSE https://github.com/OpenFTC/EasyOpenCV PARA TER MAIS INFORMAÇÕES SOBRE OS METODOS E FUNCÕES DO Easy OpenCV USADOS NESTE CÓDIGO

package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import java.lang.reflect.Array;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;
import org.opencv.core.CvType;
import org.opencv.core.Size;
import java.util.ArrayList;
import java.util.Collections;
import org.opencv.core.MatOfPoint;


public class SquareLocationDetectorOpenCV extends OpenCvPipeline {
    Telemetry telemetry;
    
    // Cria a matriz princiapal "mat" e a que é exibida na Drive Station "rusult" 
    Mat mat, result = null;
    
    // Variavel para guardar o resultado/posicao do elemento
    private Location location;
    
    public SquareLocationDetectorOpenCV(Telemetry t) { 
        telemetry = t;
        mat = new Mat();
    }
    
    // Toda parte de processamento, de cada frame e colocada aqui
    @Override
    public Mat processFrame(Mat input) {
        
        // Primeiro convertemos o video para em escala de cinza e inserimos no "mat" principal 
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_BGR2HLS);
        
        /* 
        Agora criamos o filtro. É a aqui que definimos a faixa de cor, saturação e brilho do elemento que vamos utilizar.
        Essa é uma escala HLS (Hue, Lightness e Saturation), e os valores são de 0 a 255.
        */
        Scalar lower = new Scalar (90, 85, 75);
        Scalar upper = new Scalar (120, 200, 210);
        
        Core.inRange(mat, lower, upper, mat);
        
        // As linhas a seguir são para melhorar a qualidade da imagem, para facilitar a detecção
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
   
        Imgproc.erode(mat, mat, kernel);
        Imgproc.dilate(mat, mat, kernel);
        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 10);
        
        Imgproc.erode(mat, mat, kernel);
        Imgproc.dilate(mat, mat, kernel);
        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 10);

        Imgproc.threshold(mat, mat, 20, 255, Imgproc.THRESH_BINARY);
        
        
        // Agora criamos uma lista de todos os segmentos de pixeis que contornam os elemento
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat temp = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.findContours(mat, contours, temp, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        
        if (result == null)  result = new Mat(); 
        else result.release(); 
        
        // Definimos "NOT_FOUND" como padrão caso não haja elementos validos na tela 
        location = Location.NOT_FOUND;
        
        // Caso existam elementos, procuramos o maior elemento da tela
        if (contours.size() > 0){
            double maxVal = -1;
            int maxValIdx = -1;
            
            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                double contourArea = Imgproc.contourArea(contours.get(contourIdx));
                
                // Definimos como um elemento valido se o elemento tiver uma área maior que 500
                if (contourArea > 500){
                    maxVal = contourArea;
                    maxValIdx = contourIdx;
                }
                // Se não for maior, retiramos esse elemento da lista
                else if ((contourArea <= 500) && (maxValIdx > -1)) {
                    contours.remove(contourIdx);
                }
            }
            
            // Agora substituimos as regiões claras da imagem de processo pela imagem real e colocamos em "result" para visualizarmos
            Core.bitwise_and(input, input, result, mat);        
            
            // Descomente esta linha se quiser visualizar os contornos
            //Imgproc.drawContours(result, contours, -1, new Scalar(255, 0, 0), 3);
            
            mat.release();
            
            // Se houver um elemento valido, criamos um retângulo em volta
            if (maxValIdx >= 0) {
                Rect biggestRect = Imgproc.boundingRect(new MatOfPoint(contours.get(maxValIdx).toArray()));
            
                Point supDir = new Point (biggestRect.x, biggestRect.y);
                Point botEsc = new Point (biggestRect.x + biggestRect.width, biggestRect.y + biggestRect.height);
            
                Imgproc.rectangle(result, supDir, botEsc, new Scalar(0,255,0), 5);
            
                setLocation(biggestRect.x + biggestRect.width / 2);
            }
            
        }
        // Se não houver nenhum elemento na tela, apenas exiba a imagem
        else {
            Core.bitwise_and(input, input, result, mat);        
            mat.release();
        }
        
        // Desenhamos linhas na tela para tem um feedback visual do resultado
        switch (this.getLocation()) {
            case LEFT://LEFT ~176
                Imgproc.line(result, new Point(160, 190), new Point(160, 230), new Scalar(0,255,255), 3);
                break;
            case RIGHT://RIGHT ~515
                Imgproc.line(result, new Point(490, 190), new Point(490, 230), new Scalar(0,255,255), 3);
                break;
            case CENTER://CENTER ~327
                Imgproc.line(result, new Point(312, 190), new Point(312, 230), new Scalar(0,255,255), 3);
                break;    
            case NOT_FOUND:
                Imgproc.line(result, new Point(200, 180), new Point(400, 290), new Scalar(0,255,255), 3);
                Imgproc.line(result, new Point(400, 180), new Point(200, 290), new Scalar(0,255,255), 3);
        }
        
        // Por fim, retornamos "result" para visualização na Drive Station
        return result;
    }
    
    // Aqui colocamos os limites que definem em qual posição o objeto está 
    private void setLocation(int valX) {
        if (valX < 252) {
            location = Location.LEFT;
        }
        else if (valX > 422){
            location = Location.RIGHT;
        }
        else {
            location = Location.CENTER;
        }
    }
    
    // Usamos isso para pegar a posição/resultado de detecção
    public Location getLocation() {
        return location;
    }
}
