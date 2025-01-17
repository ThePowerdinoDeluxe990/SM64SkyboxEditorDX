import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.*;

public class MainWindow extends JFrame implements ActionListener {
    byte[] rom;

    HashMap<String, Dimension> SkyBoxes = new HashMap<String, Dimension>();

    JFileChooser jfc;
    JButton openROM = new JButton("Browse");
    JTextField romLoc = new JTextField();
    JTextField address = new JTextField("B35715");
    JTextField widthF = new JTextField("256");
    JTextField heightF = new JTextField("256");
    JPanel ImagePreview = new JPanel();
    JButton importPNG = new JButton("Import (.png)");
    JButton exportPNG = new JButton("Export (.png)");
    JButton preview = new JButton("Preview");
    JButton save = new JButton("Save");
    JButton listButton = new JButton("List");
    JButton[] imageButtons = new JButton[64];
    BufferedImage[] images;
    JList list;
    JFrame listFrame;
    JFrame f;
    int width=256, height=256;
    byte[] order = new byte[320];
    boolean enabledGrid = false;
    int orderLoc;


    public MainWindow(){
        FlatLightLaf.setup();
        SkyBoxes.put("Ocean: B35715", new Dimension(256,256));
        SkyBoxes.put("Icy Mountains: B5D855", new Dimension(256,256));
        SkyBoxes.put("Sky: B85995", new Dimension(256,160));
        SkyBoxes.put("Fiery Sky: BA22D5", new Dimension(256,192));
        SkyBoxes.put("Underwater City: BC2C15", new Dimension(256,256));
        SkyBoxes.put("Clouds: BEAD55", new Dimension(256,256));
        SkyBoxes.put("Desert: C12E95", new Dimension(256,256));
        SkyBoxes.put("Dark Woods: C3AFD5", new Dimension(256,160));
        SkyBoxes.put("Dark World: C57915", new Dimension(256,256));
        SkyBoxes.put("Purple Sky: C7FA55", new Dimension(256,256));

        f = new JFrame("SM64 Skybox Importer DX v1.0");
        try {
            f.setIconImage(new ImageIcon(requireNonNull(getClass().getResource("/Assets/iconImage.png"))).getImage());
        }catch(Exception main){
            AlertApp.Erroralert("No Icon detected");
        }

        f.setLayout(new BoxLayout(f.getContentPane(),BoxLayout.Y_AXIS));


        sizeAll(ImagePreview,32*8+8,32*8+8);
        sizeAll(romLoc,250,20);
        sizeAll(openROM,60,20);
        sizeAll(address,70,20);
        sizeAll(widthF,60,20);
        sizeAll(heightF,60,20);
        sizeAll(importPNG,100,30);
        sizeAll(exportPNG,100,30);
        sizeAll(preview,70,30);
        sizeAll(save,70,30);
        sizeAll(listButton,40,20);



        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        ImagePreview.setBorder(loweredetched);
        ImagePreview.setLayout(new GridLayout(8,8,0,0));
        MakeImageButtons();

        openROM.setMargin(new Insets(0, 0, 0, 0));
        openROM.addActionListener(this);
        importPNG.setMargin(new Insets(0, 0, 0, 0));
        importPNG.addActionListener(this);
        exportPNG.setMargin(new Insets(0, 0, 0, 0));
        exportPNG.addActionListener(this);
        preview.setMargin(new Insets(0, 0, 0, 0));
        preview.addActionListener(this);
        save.setMargin(new Insets(0, 0, 0, 0));
        save.addActionListener(this);
        listButton.setMargin(new Insets(0, 0, 0, 0));
        listButton.addActionListener(this);

        JPanel ch = new JPanel();
        sizeAll(ch,32*8,40);
        ch.setBorder(loweredetched);

        f.add(groupComponents(new Component[]{new JLabel("ROM: "),romLoc,openROM}));
        f.add(groupComponents(new Component[]{ImagePreview}));
        f.add(groupComponents(new Component[]{
                MakeButton("◄◄","Shift Image Left"),
                MakeButton("▲▲","Shift Image Up"),
                MakeButton("▼▼","Shift Image Down"),
                MakeButton("►►","Shift Image Right"),
                MakeButton("#","Show Grid")
        }));
        f.add(groupComponents(new Component[]{
                new JLabel("Address: "),address,
                new JLabel("Width: "),widthF,
                new JLabel("Height: "),heightF, listButton
        }));
        f.add(groupComponents(new Component[]{importPNG,preview,save,exportPNG}));

        f.setPreferredSize(new Dimension(380,440));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(true);
        f.pack();
        f.setVisible(true);
        f.setLocationRelativeTo(null);
        setupPrePanel();
        jfc = new JFileChooser();
    }


    private JButton MakeButton(String text, String tooltip){
        JButton b = new JButton(text);
        b.setToolTipText(tooltip);
        b.addActionListener(this);
        return b;
    }

    private void MakeImageButtons(){
        for(int i = 0; i < imageButtons.length; i++){
            imageButtons[i] = new JButton();
            sizeAll(imageButtons[i],32,32);
            imageButtons[i].setBorderPainted(false);
            imageButtons[i].setBorder(null);
            imageButtons[i].addActionListener(this);
            imageButtons[i].setActionCommand(i+"");
            imageButtons[i].setMargin(new Insets(0, 0, 0, 0));
            imageButtons[i].setContentAreaFilled(false);
            imageButtons[i].setRolloverEnabled(false);
            ImagePreview.add(imageButtons[i]);
        }
    }

    private JPanel groupComponents(Component[] c){
        JPanel jp = new JPanel();
        int w = 0, h = 0;
        for(Component comp: c){
            jp.add(comp);
            w += comp.getPreferredSize().width*1.5;
            h = Math.max(comp.getPreferredSize().height, h);
        }
        sizeAll(jp,w+20,h+10);
        return jp;
    }

    private void sizeAll(Component c, int x, int y){
        c.setPreferredSize(new Dimension(x,y));
        c.setMaximumSize(new Dimension(x,y));
        c.setMinimumSize(new Dimension(x,y));
    }

    private byte[] correctOrder(){
        byte[] o = new byte[256];
        int count = 0;
        for(int i = 0; i < 80; i++){
            if(i%10==8||i%10==9) continue;
            o[count] = order[i*4];
            o[count+1] = order[i*4+1];
            o[count+2] = order[i*4+2];
            o[count+3] = order[i*4+3];
            count+=4;
        }
        return o;
    }
    JPanel p = new JPanel();
    JLabel pImg = new JLabel();
    JComboBox Images;

    String test = "null";
    int currentImg = 0;
    boolean isChanging = false;
    byte[] corder = correctOrder();


    private void setupPrePanel(){
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        Images = new JComboBox();
        sizeAll(Images,216,25);
        Images.addActionListener(this);
        p.add(Images);
        p.add(Box.createRigidArea(new Dimension(3,0)));
        p.add(pImg);
        p.setBackground(new Color(200,200,200));
        p.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        sizeAll(p,256,40);
    }

    private String IntToOrderString(int a){
        String str = "0A";
        byte[] b = IntToBytes(a);
        String v = Integer.toHexString(b[1]&0xFF);
        if(v.length()==1)v="0"+v; str+=v;
        v = Integer.toHexString(b[2]&0xFF);
        if(v.length()==1)v="0"+v; str+=v;
        v = Integer.toHexString(b[0]&0xFF);
        if(v.length()==1)v="0"+v; str+=v;
        return fix(str);
    }

    private void updatePrePanel(int img){
        Images.removeAllItems();
        int c = (width/32)*(height/32);
        for(int i = 0; i < 64; i++){
            if(i <= c) Images.insertItemAt(IntToOrderString(i*2048),i);
            else Images.insertItemAt(IntToOrderString(c*2048),i);
        }
        Images.setSelectedIndex(img);
        pImg.setIcon(imageButtons[img].getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()){
            case "Browse":
                jfc.resetChoosableFileFilters();
                jfc.setFileFilter(new FileNameExtensionFilter(".z64 File", "z64"));
                jfc.setAcceptAllFileFilterUsed(false);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    romLoc.setText(file.getAbsolutePath());
                    try {
                        LoadRom(file);
                        RenderImage();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case "List":
                listFrame = List();
                break;
            case "Preview":
                if (rom != null) RenderImage();
                else{
                    AlertApp.Erroralert("No rom detected");
                }
                break;
            case "Ok":
                if (list != null){
                    if (list.getSelectedValue() != null){
                        String v = list.getSelectedValue().toString();
                        String[] split = v.substring(v.indexOf(":")+2,v.length()).split(" ");
                        address.setText(split[0]);
                        String[] size = split[1].split("x");
                        widthF.setText(size[0]);
                        heightF.setText(size[1]);
                    }
                    listFrame.dispose();
                    if (rom != null) RenderImage();
                }
                break;
            case "Export (.png)":
                jfc.resetChoosableFileFilters();
                jfc.setFileFilter(new FileNameExtensionFilter(".png Image", "png"));
                jfc.setAcceptAllFileFilterUsed(false);
                jfc.setSelectedFile(new File("Skybox.png"));
                if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    try {
                        ImageIO.write(exportImg(), "PNG", file);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case "Import (.png)":
                jfc.resetChoosableFileFilters();
                jfc.setFileFilter(new FileNameExtensionFilter(".png Image", "png"));
                jfc.setAcceptAllFileFilterUsed(false);
                jfc.setSelectedFile(new File(""));
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    ImportTexture(file);
                }
                break;
            case "Save":
                SaveRom();
                break;
            case "#":
                if(rom == null){
                    AlertApp.Erroralert("No rom detected");
                }else{
                    if(enabledGrid){
                        ImagePreview.setLayout(new GridLayout(8,8,0,0));
                        enabledGrid = false;
                    } else {
                        ImagePreview.setLayout(new GridLayout(8,8,1,1));
                        enabledGrid = true;
                    }
                    ImagePreview.revalidate();
                }


                break;
            case "◄◄": shift(order,-1,true);MarkDirty(); break;
            case "►►": shift(order,1,true);MarkDirty(); break;
            case "▲▲": shift(order,-1,false);MarkDirty(); break;
            case "▼▼": shift(order,1,false);MarkDirty();  break;

            case "comboBoxChanged":
                if(rom == null){
                    AlertApp.Erroralert("No rom detected");
                }else {
                    if (Images.getSelectedItem() != null && !isChanging) {
                        byte[] orderBytes = IntToBytes(Integer.valueOf(Images.getSelectedItem().toString(), 16));
                        ByteBuffer bb = ByteBuffer.wrap(rom);
                        int pos = currentImg;
                        pos += (pos / 8) * 2;
                        bb.position(orderLoc + (pos * 4));
                        bb.put(orderBytes);
                        RenderImage();
                        pImg.setIcon(imageButtons[currentImg].getIcon());
                        MarkDirty();
                        return;
                    }
                }
                break;
        }

        try{
            isChanging = true;
            int img = Integer.parseInt(e.getActionCommand());
            currentImg = img;
            if(test.equals(img+"")){test="null"; f.remove(p); f.setSize(380,440);}
            else if(test.equals("null")){test=img+"";updatePrePanel(img); f.add(p,2);f.setSize(380,480);}
            else{ test = img+"";updatePrePanel(img);}
            f.revalidate();
            isChanging = false;
        }catch(NumberFormatException ex){}
    }

    private void shift(byte[] b, int shift, boolean horz){
        Integer[][] map;
        if(horz){
            map = new Integer[8][8];

            for(int i = 0; i < order.length/4; i++){
                if(i%10 == 8 || i%10 == 9) continue;
                map[i/10][i%10] = BytesToInt(order,(int)i*4);
            }

            for(int i = 0; i < 8; i++){
                java.util.List l = new ArrayList(Arrays.asList(map[i]));
                Collections.rotate(l,shift);
                l.add(l.get(0));
                l.add(l.get(1));
                map[i] = (Integer[])l.toArray(map[i]);
            }
        } else {
            map = new Integer[8][10];
            for(int i = 0; i < order.length/4; i++){
                map[i/10][i%10] = BytesToInt(order,(int)i*4);
            }

            List l = new ArrayList(Arrays.asList(map));
            Collections.rotate(l,shift);
            map = (Integer[][])l.toArray(map);
        }
        byte[] bytes = new byte[0x140];
        int count = 0;
        for(int i = 0; i < 8; i++){
            for(int j =0; j < 10; j++){
                byte[] temp = IntToBytes(map[i][j]);
                bytes[count] = temp[0];
                bytes[count+1] = temp[1];
                bytes[count+2] = temp[2];
                bytes[count+3] = temp[3];
                count+=4;
            }
        }

        if(rom == null){
            AlertApp.Erroralert("No rom detected");
        }else {
            ByteBuffer bb = ByteBuffer.wrap(rom);
            bb.position(orderLoc);
            bb.put(bytes);

            RenderImage();
        }
    }

    private BufferedImage exportImg(){
        BufferedImage bi = new BufferedImage(248,248,BufferedImage.TYPE_INT_ARGB);

        for(int y = 0; y < 8;y++){
            for(int x = 0; x < 8;x++){
                BufferedImage mImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                mImage.getRaster().setPixels(0, 0, 32, 32, getChunk(y*8+x));
                Graphics2D gr = bi.createGraphics();
                gr.drawImage(mImage,31 * x, 31 * y, 31 * x + 31, 31 * y + 31,0,0,31,31, null);
                gr.dispose();
            }
        }
        return bi;
    }

    private int[] getChunk(int c){
        try{
            return chunk[c];
        } catch(Exception e){
            return chunk[(width/32)*(height/32)];
        }
    }


    private JFrame List(){
        JFrame fr = new JFrame("Skybox Presets");
        fr.setLayout(new BoxLayout(fr.getContentPane(),BoxLayout.Y_AXIS));
        sizeAll(fr, 250, 250);

        String[] data = new String[SkyBoxes.size()];
        int c = 0;
        Iterator itr = SkyBoxes.values().iterator();
        Iterator itr2 = SkyBoxes.keySet().iterator();
        while(itr.hasNext()) {
            Dimension e1 = (Dimension)itr.next();
            Object e2 = itr2.next();
            data[c] = e2 + " "+(int)e1.getWidth()+"x"+(int)e1.getHeight();
            c++;
        }
        list = new JList(data); //data has type Object[]
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        sizeAll(list,100,SkyBoxes.size()*20);
        //list.setVisibleRowCount(-1);

        JButton ok = new JButton("Ok");
        ok.addActionListener(this);
        //sizeAll(ok,150,25);

        JScrollPane sp = new JScrollPane(list);
        sp.setWheelScrollingEnabled(true);
        fr.add(sp);
        fr.add(groupComponents(new Component[]{ok}));
        fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fr.setResizable(false);
        fr.pack();
        fr.setVisible(true);
        fr.setLocationRelativeTo(null);
        fr.setAlwaysOnTop(true);
        return fr;
    }
    int[][] chunk;
    private void RenderImage(){
        width = Integer.parseInt(widthF.getText());
        height = Integer.parseInt(heightF.getText());
        byte extra = 1;
        if  (height > 224) extra = 0;

        int start = Integer.parseInt(address.getText(),16);
        int end = start+(width*height*2)+(0x800*extra);
        byte[] data = Arrays.copyOfRange(rom, start, end);

        int numOfChunks = (width/32)*(height/32)+(1*extra);
        chunk = new int[numOfChunks][4096];
        int c = 0;
        for(int i=0; i<numOfChunks*2048;i+=2){
            int a = i/2048;
            short LoadRGBA_RGBA5551 = (short)(((data[i]&0xFF) * 0x100) + (data[i+1]&0xFF));
            chunk[a][c%4096] = (((LoadRGBA_RGBA5551 >> 11)&0x1F)*0xFF)/0x1F;
            chunk[a][c%4096+1] = (((LoadRGBA_RGBA5551 >> 6)&0x1F)*0xFF)/0x1F;
            chunk[a][c%4096+2] = (((LoadRGBA_RGBA5551 >> 1)&0x1F)*0xFF)/0x1F;
            chunk[a][c%4096+3] = (byte)((LoadRGBA_RGBA5551 & 1) * 0xFF);
            //System.out.println(chunk[a][c%4096+1]+","+chunk[a][c%4096+2]+","+chunk[a][c%4096+3]);
            c+=4;
        }

        order = Arrays.copyOfRange(rom, end, end+0x140);
        corder = correctOrder();
        orderLoc = end;
        // PrintHexBytes(order,order.length);
        BufferedImage[] imgs = new BufferedImage[numOfChunks];
        for (int i = 0; i < numOfChunks; i++) {
            BufferedImage mImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            mImage.getRaster().setPixels(0, 0, 32, 32, chunk[i]);
            imgs[i] = resize(mImage,64,64,false).getSubimage(1, 1, 63, 63);
        }
        images = imgs;

        byte count = 0;
        for(int i = 0; i < 80; i++){
            if(i%10==8||i%10==9)continue;
            int place = (BytesToInt(order,(int)i*4) << 8)>>8;
            imageButtons[count].setIcon(new ImageIcon(resize(imgs[place/0x800],32,32,false)));
            imageButtons[count].setFocusPainted(false);
            BufferedImage b = resize(imgs[place/0x800],32,32,false);
            RescaleOp op = new RescaleOp(1.5f, 100, null);
            b = op.filter(b, b);
            imageButtons[count].setRolloverIcon(new ImageIcon(b));
            count++;
        }

    }

    private BufferedImage resize(BufferedImage img, int newW, int newH, boolean special) {
        Image tmp;
        if(special)tmp = img.getSubimage(0, 0, newW+8, newH+8).getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        else tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private int BytesToInt(byte[] buffer, int startAddress){
        return ByteBuffer.wrap(buffer).getInt(startAddress);
    }
    private  byte[] IntToBytes(int anInt){
        return ByteBuffer.allocate(4).putInt(anInt).array();
    }
    private String fix(String f){
        if (f.length() == 1) return "0"+f.toUpperCase();
        else return f.toUpperCase();
    }

    private void ImportTexture(File file){
        width = Integer.parseInt(widthF.getText());
        height = Integer.parseInt(heightF.getText());
        int origHeight = 0;
        BufferedImage img = null;
        BufferedImage img2 = null;
        try {
            img = ImageIO.read(file);
            origHeight = img.getHeight();
            if (img.getWidth()%31 == 0 && img.getHeight()%31 == 0){
                int h = img.getHeight()/31;
                if(img.getHeight()>height){
                    img2 = img.getSubimage(0, height-h, 248, (img.getHeight())-(height-h));
                    img2 = UpscaleImg(img2,width,(img2.getHeight()+h));
                }
                img = UpscaleImg(img,width,height);
            }
            else if (img.getWidth()%32 == 0 && img.getHeight()%32 == 0) {
                if(img.getHeight()>height){
                    img2 = img.getSubimage(0, height, 256, img.getHeight()-height);
                }
                img = resize(img,width-8,height-8,true);
                img = UpscaleImg(img,width,height);
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Both the width & height of the imported image should be divisible by 31 or 32",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        } catch (IOException e) {}

        if (img != null){

            int rows = width/32;
            int cols = height/32;
            int chunks = rows * cols;
            int extra = 0;

            if (cols < 8 && origHeight > height){
                chunks+=1;
                extra=0x800;
            }

            int count = 0;
            BufferedImage[] imgs = new BufferedImage[chunks];
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    imgs[count] = new BufferedImage(32, 32, img.getType());
                    Graphics2D gr = imgs[count++].createGraphics();
                    gr.drawImage(img, 0, 0, 32, 32, 32 * y, 32 * x, 32 * y + 32, 32 * x + 32, null);
                    gr.dispose();
                }
            }

            if (img2!=null){
                imgs[chunks-1] = new BufferedImage(32, 32, img2.getType());
                Graphics2D gr = imgs[chunks-1].createGraphics();
                gr.drawImage(img2, 0, 0, 32, 32,0,0,32,32, null);
                gr.dispose();
            }

        /*
        JFrame frame = new JFrame();
        frame.setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.Y_AXIS));
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        if(img2!=null)frame.getContentPane().add(new JLabel(new ImageIcon(img2)));
        frame.pack();
        frame.setVisible(true);
        */
            byte[] rgba5 = new byte[width*height*2+extra];
            int l = 0;
            for (int i = 0; i < rows*cols+(extra/0x800); i++){
                BufferedImage output = imgs[i];
                for (int y = 0; y < 32; y++) {
                    for (int x = 0; x < 32; x++) {
                        try {
                            Color c = new Color(output.getRGB(x, y), true);
                            short conv = convertTo5551(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
                            byte[] convB = ByteBuffer.allocate(2).putShort(conv).array();
                            rgba5[l] = convB[0];
                            rgba5[l+1] = convB[1];
                            l+=2;
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            int start = Integer.parseInt(address.getText(),16);
            ByteBuffer bb = ByteBuffer.wrap(rom);
            bb.position(start);
            bb.put(rgba5);
            //ImagePreview.setIcon(new ImageIcon(output));
            SaveRom();
        }
    }
    private void MarkDirty(){
        save.setForeground(Color.red);
    }
    private void SaveRom(){
        // Save data to the rom
        if(rom == null){
            AlertApp.Erroralert("No rom detected");
        }else {
            save.setForeground(Color.blue);
            save.setText("Saving...");
            RenderImage();
            Runnable r = new SaveBytes(rom, romLoc.getText());


            new Thread(r).start();
        }

    }

    private void LoadRom(File file) throws IOException {
        int size = (int)file.length();
        if (size<0) { // Probably never happens.
            throw new IOException("Negative file length: " + size);
        }
        rom = new byte[size];

        if (size>0) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            int pos = 0;
            int count = 0;
            try {
                while (pos<rom.length &&(count=in.read(rom, pos, rom.length-pos))>-1) {
                    pos += count;
                }
            } finally {
                in.close();
            }
        }
    }

    /*
    Special thanks to Kaze for this function.
    Link: http://smwc.me/1209817
    */
    BufferedImage UpscaleImg(BufferedImage orig, int width, int height){
        BufferedImage converted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int rows = width/32;
        int cols = height/32;
        boolean qf = true;
        if(rows*cols > 63) qf = false;
        for (int y = 0; y < cols; y ++) {
            for (int x = 0; x < rows; x++) {
                for (int inY = 0; inY < 32; inY++) {
                    for (int inX = 0; inX < 32; inX++) {
                        if(qf && (y == cols-1 && inY == 31))
                            converted.setRGB(inX+32*x, inY+32*y,orig.getRGB((inX+31*x)%orig.getWidth(), (inY+31*y-1)%orig.getHeight()));
                        else if(qf && (y == cols-1 && inY == 0))
                            converted.setRGB(inX+32*x, inY+32*y,orig.getRGB((inX+31*x)%orig.getWidth(), (inY+31*y+1)%orig.getHeight()));
                        else
                            converted.setRGB(inX+32*x, inY+32*y,orig.getRGB((inX+31*x)%orig.getWidth(), (inY+31*y)%orig.getHeight()));
                    }
                }
            }
        }
        return converted;
    }

    short convertTo5551(int r, int g, int b, int a) throws IOException {
        int r5 = (r+8) * 31 / 255;
        int g5 = (g+8) * 31 / 255;
        int b5 = (b+8) * 31 / 255;
        int a1 = (a > 0) ? 1 : 0;
        int rShift = r5 << 11;
        int gShift = g5 << 6;
        int bShift = b5 << 1;

        // Combine and return
        return (short) (rShift | gShift | bShift | a1);
    }



    public class SaveBytes implements Runnable {

        byte[] bytes;
        String fileName;

        public SaveBytes(byte[] newBytes, String filename){
            bytes = newBytes;
            fileName = filename;
        }


        @Override
        public void run() {
            if (bytes != null && fileName != null){

                try (OutputStream out = new FileOutputStream(fileName)) {
                    out.write(bytes, 0, bytes.length);
                } catch (IOException ex) {
                    Logger.getLogger(SaveBytes.class.getName()).log(Level.SEVERE, null, ex);
                }

                save.setText("Save");
                save.setForeground(Color.black);
                //System.out.println("Done!");
            }
        }

    }

}
