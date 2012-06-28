package org.geotools.tutorial.raster;

import it.geosolutions.imageio.plugins.tiff.TIFFImageWriteParam;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.stream.ImageOutputStream;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.Warp;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.transform.WarpTransform2D;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.style.ContrastMethod;

public class ImageLab {

    private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    private JMapFrame frame;
    private AbstractGridCoverage2DReader reader;

    public static void main(String[] args) throws Exception {
        ImageLab me = new ImageLab();
        me.getLayersAndDisplay();
    }
    private Object Viewer;
    
    
    
/**
     * Prompts the user for a GeoTIFF file and a Shapefile and passes them to the displayLayers
     * method
     */
    public void getLayersAndDisplay() throws Exception {
        List<Parameter<?>> list = new ArrayList<Parameter<?>>();
        list.add(new Parameter<File>("image1", File.class, "Image",
                "GeoTiff or World+Image to display as basemap",
                new KVP( Parameter.EXT, "tif", Parameter.EXT, "jpg")));
         list.add(new Parameter<File>("image2", File.class, "Image",
                "GeoTiff or World+Image to display as basemap",
                new KVP( Parameter.EXT, "tif", Parameter.EXT, "jpg")));
        
        JParameterListWizard wizard = new JParameterListWizard("Image Lab",
                "Fill in the following layers", list);
        int finish = wizard.showModalDialog();

        if (finish != JWizard.FINISH) {
            System.exit(0);
        }
        
        //displayLayers(imageFile1, imageFile2);
                
        String ref_exe_path="/home/j/opencv/corner "+wizard.getConnectionParameters().get("image1")+" "+wizard.getConnectionParameters().get("image2");
        String userInput;
        String userInput2;
        
        Runtime r;
           r = Runtime.getRuntime();
           System.out.println("About to exec args");
           Process p = r.exec(ref_exe_path);
           System.out.println("did exec, about to read stdout");
           BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
           BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        
        // read the output from the command
        

        String[] dp = new String[1000];
        int nn=0;
        while ((userInput=stdInput.readLine()) != null) {
              dp[nn]=userInput;
              System.out.println(dp[nn]);
              nn=nn+1;
        }
        System.out.println(nn);
        
        // read any errors from the attempted command
        /*System.out.println("Here is the standard error of the command (if any):\n");
        while ((stdError.readLine()) != null) {
            System.out.println(stdError.readLine());
        }
        */
        File imageFile1 = (File) wizard.getConnectionParameters().get("image1");
        //File imageFile2 = (File) wizard.getConnectionParameters().get("image2");
        AbstractGridFormat format = GridFormatFinder.findFormat(imageFile1);
        ParameterValueGroup gtparams=format.getReadParameters();
        List<GeneralParameterValue> gp=gtparams.values();
        AbstractGridCoverage2DReader areader = format.getReader(imageFile1);
        GridCoverage2D gc=areader.read(null);
        CoordinateReferenceSystem CRS = gc.getCoordinateReferenceSystem2D();
        MathTransform Transform = gc.getGridGeometry().getGridToCRS();
        Point2D[] src = new Point2D[nn/4];
        Point2D[] dest = new Point2D[nn/4];
        String gcp="";
        for (int i=0;i<nn/4;i++){            
            double x=Double.valueOf(dp[4*i]);//error
            double y=Double.valueOf(dp[4*i+1]);
            double x_src=Double.valueOf(dp[4*i+2]);//error
            double y_src=Double.valueOf(dp[4*i+3]);
            DirectPosition2D p2 = new DirectPosition2D(x,y);
            DirectPosition p2_result = null;
            p2_result=Transform.transform(p2.getDirectPosition(),p2_result);
            double[] proj=p2_result.getCoordinate();
            //System.out.println(proj[0]);
            //System.out.println(proj[1]);
            //src[i]  = new DirectPosition2D(x_src,y_src);
            //dest[i] = new DirectPosition2D(proj[0], proj[1]);
            gcp=gcp+" -gcp "+x_src+" "+y_src+" "+proj[0]+" "+proj[1];
        }
        
        String translate_exe="gdal_translate"+gcp+" -of GTiff "+wizard.getConnectionParameters().get("image2")+" output.tif";
        System.out.println(translate_exe);
        Runtime r_translate;
           r_translate = Runtime.getRuntime();           
           Process r_t=r_translate.exec(translate_exe);
           r_t.waitFor();

           String warp_exe="gdalwarp -t_srs EPSG:26916 output.tif result_geo.tif";
        Runtime r_warp;
            r_warp = Runtime.getRuntime();            
            Process r_w=r_warp.exec(warp_exe);
            BufferedReader stdInput2 = new BufferedReader(new InputStreamReader(r_w.getInputStream()));
            BufferedReader stdError2 = new BufferedReader(new InputStreamReader(r_w.getErrorStream()));
            String[] dp2 = new String[1000];
        int nn2=0;
        while ((userInput2=stdInput2.readLine()) != null) {
              dp2[nn2]=userInput2;
              System.out.println(dp2[nn2]);
              nn2=nn2+1;
        }
        
        /*int deg = 1;

        WarpTransform2D wt = new WarpTransform2D(src, dest, deg);
        //String img2=""+wizard.getConnectionParameters().get("image2");
        //System.out.println(img2);
        //InputStream is = getClass().getResourceAsStream(img2);
        //BufferedImage sourceImage = ImageIO.read((File)wizard.getConnectionParameters().get("image2"));
        BufferedImage sourceImage = ImageIO.read(new File("/home/j/opencv/crawford_1950.TIF"));

        Warp warp = WarpTransform2D.getWarp("", wt);
        ParameterBlock paramBlk = new ParameterBlock().addSource(sourceImage);
        paramBlk = paramBlk.add(warp);
        JAI processor = JAI.getDefaultInstance();
        String operation = "Warp";
        RenderingHints targetHints = new RenderingHints(null);
        RenderedOp tImage = processor.createNS(operation, paramBlk,targetHints);
        //boolean write = ImageIO.write(tImage, "tif", new File("/home/j/opencv/corner/result.tif"));
        //PlanarImage myImageOp = JAI.create("FileLoad", srcImgFile);


        String dstImgFile="result.tiff";
        String dstFileType="TIFF";
        JAI.create("filestore", tImage, dstImgFile, dstFileType);
        */
        //saveTiff("/home/j/opencv/corner/result.tif",tImage);

    }
    
    protected boolean saveTiff(String filename, RenderedOp image) {

File tiffFile = new File(filename);
ImageOutputStream ios = null;
ImageWriter writer = null;

try {

// find an appropriate writer
Iterator it = ImageIO.getImageWritersByFormatName("TIF");
if (it.hasNext()) {
writer = (ImageWriter)it.next();
} else {
return false;
}

// setup writer
ios = ImageIO.createImageOutputStream(tiffFile);
writer.setOutput(ios);
TIFFImageWriteParam writeParam = new TIFFImageWriteParam(Locale.ENGLISH);
writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
// see writeParam.getCompressionTypes() for available compression type strings
writeParam.setCompressionType("PackBits");

// convert to an IIOImage
IIOImage iioImage = new IIOImage(image.getAsBufferedImage(), null, null);

// write it!
writer.write(null, iioImage, writeParam);

} catch (IOException e) {
return false;
}
return true;

}
    /**
     * Displays a GeoTIFF file overlaid with a Shapefile
     *
     * @param rasterFile
     *            the GeoTIFF file
     * @param shpFile
     *            the Shapefile
     */
    private void displayLayers(File rasterFile, File shpFile) throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat( rasterFile );
        reader = format.getReader(rasterFile);

        // Initially display the raster in greyscale using the
        // data from the first image band
        Style rasterStyle = createGreyscaleStyle(1);

        // Connect to the shapefile
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);
        SimpleFeatureSource shapefileSource = dataStore
                .getFeatureSource();

        // Create a basic style with yellow lines and no fill
        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        // Set up a MapContext with the two layers
        final MapContext map = new DefaultMapContext();
        map.setTitle("ImageLab");
        map.addLayer(reader, rasterStyle);
        map.addLayer(shapefileSource, shpStyle);

        // Create a JMapFrame with a menu to choose the display style for the
        frame = new JMapFrame(map);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
        //frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
        frame.enableToolBar(true);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu menu = new JMenu("Raster");
        menuBar.add(menu);

        menu.add( new SafeAction("Grayscale display") {
            public void action(ActionEvent e) throws Throwable {
                Style style = createGreyscaleStyle();
                if (style != null) {
                    map.getLayer(0).setStyle(style);
                    frame.repaint();
                }
            }
        });

        menu.add( new SafeAction("RGB display") {
            public void action(ActionEvent e) throws Throwable {
                Style style = createRGBStyle();
                if (style != null) {
                    map.getLayer(0).setStyle(style);
                    frame.repaint();
                }
           }
        });
        // Finally display the map frame.
        // When it is closed the app will exit.
        frame.setVisible(true);
    }

    /**
     * Create a Style to display a selected band of the GeoTIFF image
     * as a greyscale layer
     *
     * @return a new Style instance to render the image in greyscale
     */
    private Style createGreyscaleStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        int numBands = cov.getNumSampleDimensions();
        Integer[] bandNumbers = new Integer[numBands];
        for (int i = 0; i < numBands; i++) { bandNumbers[i] = i+1; }
        Object selection = JOptionPane.showInputDialog(
                frame,
                "Band to use for greyscale display",
                "Select an image band",
                JOptionPane.QUESTION_MESSAGE,
                null,
                bandNumbers,
                1);
        if (selection != null) {
            int band = ((Number)selection).intValue();
            return createGreyscaleStyle(band);
        }
        return null;
    }


    /**
     * Create a Style to display the specified band of the GeoTIFF image
     * as a greyscale layer.
     * <p>
     * This method is a helper for createGreyScale() and is also called directly
     * by the displayLayers() method when the application first starts.
     *
     * @param band the image band to use for the greyscale display
     *
     * @return a new Style instance to render the image in greyscale
     */
    private Style createGreyscaleStyle(int band) {
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    /**
     * This method examines the names of the sample dimensions in the provided coverage looking for
     * "red...", "green..." and "blue..." (case insensitive match). If these names are not found
     * it uses bands 1, 2, and 3 for the red, green and blue channels. It then sets up a raster
     * symbolizer and returns this wrapped in a Style.
     *
     * @return a new Style object containing a raster symbolizer set up for RGB image
     */
    private Style createRGBStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = { -1, -1, -1 };
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

}