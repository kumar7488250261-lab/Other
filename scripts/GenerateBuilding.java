import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateBuilding {
    public static void main(String[] args) throws Exception {
        int width = 1080;
        int height = 1920;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Load Noto Sans Devanagari font
        File fontFile = new File("/usr/share/fonts/truetype/noto/NotoSansDevanagari.ttf");
        Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);

        // Sky & Atmosphere (Indian Railways Station Sky)
        GradientPaint skyGrad = new GradientPaint(0, 0, new Color(15, 32, 67), 0, 800, new Color(70, 110, 160));
        g2d.setPaint(skyGrad);
        g2d.fillRect(0, 0, width, height);

        // Ground / Platform
        GradientPaint groundGrad = new GradientPaint(0, 1200, new Color(40, 48, 56), 0, height, new Color(20, 24, 30));
        g2d.setPaint(groundGrad);
        g2d.fillRect(0, 1200, width, height - 1200);

        // Railway tracks / Platform edge
        g2d.setColor(new Color(235, 175, 40));
        g2d.fillRect(0, 1200, width, 12); // Yellow safety tactile line
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillRect(0, 1215, width, 6); // White platform boundary

        // Building Facade (SECR Kharsia Lobby - Modern Railway Administrative Architecture)
        int bX = 90;
        int bY = 240;
        int bW = 900;
        int bH = 960;

        // Building Shadow
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillRoundRect(bX - 15, bY + 15, bW + 30, bH + 20, 24, 24);

        // Main Building Structure - Crisp SECR Railway Buff/Off-White & Terracotta/Deep Navy Accent
        g2d.setColor(new Color(245, 243, 238));
        g2d.fillRoundRect(bX, bY, bW, bH, 20, 20);

        // Building Base / Granite plinth
        g2d.setColor(new Color(90, 45, 30)); // Red brick / railway terracotta base
        g2d.fillRect(bX, bY + bH - 120, bW, 120);

        // Decorative Architectural Cornice / Top Parapet
        g2d.setColor(new Color(15, 40, 95)); // Deep Railway Blue Parapet
        g2d.fillRoundRect(bX, bY, bW, 70, 16, 16);
        g2d.fillRect(bX, bY + 50, bW, 20);

        // Central Portico / Canopy projection
        int pX = bX + 60;
        int pY = bY + 120;
        int pW = bW - 120;
        int pH = 340;

        // Canopy background (Clean Cream with Navy Border)
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillRoundRect(pX, pY, pW, pH, 16, 16);
        g2d.setColor(new Color(210, 215, 225));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(pX, pY, pW, pH, 16, 16);

        // --- MAIN SIGNBOARD (Yellow Background with Bold Black & Red Railway Signboard Typography) ---
        int sbX = pX + 20;
        int sbY = pY + 25;
        int sbW = pW - 40;
        int sbH = 160;

        // Signboard Glow / Border
        g2d.setColor(new Color(10, 30, 70));
        g2d.fillRoundRect(sbX - 6, sbY - 6, sbW + 12, sbH + 12, 18, 18);

        // Bright Railway Yellow Signboard (Standard Indian Railways Station Signboard Color)
        g2d.setColor(new Color(255, 210, 0)); // Pure IR Yellow #FFD200
        g2d.fillRoundRect(sbX, sbY, sbW, sbH, 14, 14);

        // Inner border
        g2d.setColor(new Color(0, 0, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(sbX + 4, sbY + 4, sbW - 8, sbH - 8, 10, 10);

        // HINDI TEXT: "संयुक्त चालक एवं परिचालक लॉबी"
        Font hindiTitleFont = baseFont.deriveFont(Font.BOLD, 42f);
        g2d.setFont(hindiTitleFont);
        g2d.setColor(new Color(15, 15, 20));
        String hindiText1 = "संयुक्त चालक एवं परिचालक लॉबी";
        FontMetrics fm1 = g2d.getFontMetrics(hindiTitleFont);
        int text1X = sbX + (sbW - fm1.stringWidth(hindiText1)) / 2;
        int text1Y = sbY + 58;
        g2d.drawString(hindiText1, text1X, text1Y);

        // HINDI STATION NAME: "खरसिया" (Bold Maroon / Red)
        Font hindiStationFont = baseFont.deriveFont(Font.BOLD, 54f);
        g2d.setFont(hindiStationFont);
        g2d.setColor(new Color(180, 10, 10)); // Deep Crimson Red
        String hindiStation = "खरसिया";
        FontMetrics fmStation = g2d.getFontMetrics(hindiStationFont);
        int stationX = sbX + (sbW - fmStation.stringWidth(hindiStation)) / 2;
        int stationY = sbY + 115;
        g2d.drawString(hindiStation, stationX, stationY);

        // SUB-BOARD (English & Division): "KHARSIA • SECR BILASPUR DIVISION"
        Font engFont = new Font("SansSerif", Font.BOLD, 22);
        g2d.setFont(engFont);
        g2d.setColor(new Color(20, 20, 30));
        String engText = "COMBINED CREW & TM LOBBY • KHARSIA (SECR)";
        FontMetrics fmEng = g2d.getFontMetrics(engFont);
        int engX = sbX + (sbW - fmEng.stringWidth(engText)) / 2;
        int engY = sbY + 146;
        g2d.drawString(engText, engX, engY);

        // --- SECONDARY BANNER (Under Canopy): "दक्षिण पूर्व मध्य रेलवे • बिलासपुर मंडल" ---
        int bnrX = pX + 40;
        int bnrY = pY + 200;
        int bnrW = pW - 80;
        int bnrH = 55;

        g2d.setColor(new Color(14, 45, 90)); // Navy banner
        g2d.fillRoundRect(bnrX, bnrY, bnrW, bnrH, 10, 10);

        Font hindiDivFont = baseFont.deriveFont(Font.BOLD, 26f);
        g2d.setFont(hindiDivFont);
        g2d.setColor(new Color(255, 235, 130)); // Bright yellow
        String divText = "दक्षिण पूर्व मध्य रेलवे • बिलासपुर मंडल";
        FontMetrics fmDiv = g2d.getFontMetrics(hindiDivFont);
        int divX = bnrX + (bnrW - fmDiv.stringWidth(divText)) / 2;
        int divY = bnrY + 38;
        g2d.drawString(divText, divX, divY);

        // English sub-text
        Font subEngFont = new Font("SansSerif", Font.BOLD, 18);
        g2d.setFont(subEngFont);
        g2d.setColor(new Color(80, 95, 120));
        String subEng = "SOUTH EAST CENTRAL RAILWAY • BILASPUR DIVISION";
        FontMetrics fmSubEng = g2d.getFontMetrics(subEngFont);
        int subEngX = pX + (pW - fmSubEng.stringWidth(subEng)) / 2;
        int subEngY = pY + 300;
        g2d.drawString(subEng, subEngX, subEngY);

        // Modern Glass Windows & Architecture
        g2d.setColor(new Color(35, 65, 100)); // Tinted glass
        int winY1 = bY + 500;
        int winW = 140;
        int winH = 180;
        for (int i = 0; i < 4; i++) {
            int winX = bX + 60 + i * 200;
            g2d.fillRoundRect(winX, winY1, winW, winH, 8, 8);
            g2d.setColor(new Color(180, 220, 255, 90));
            g2d.fillRect(winX + 10, winY1 + 10, 30, winH - 20); // Reflection
            g2d.setColor(new Color(35, 65, 100));
        }

        // Entrance Doors
        int doorX = bX + bW / 2 - 120;
        int doorY = bY + bH - 260;
        int doorW = 240;
        int doorH = 260;
        g2d.setColor(new Color(25, 40, 60));
        g2d.fillRoundRect(doorX, doorY, doorW, doorH, 12, 12);
        // Glass panels in doors
        g2d.setColor(new Color(70, 115, 160));
        g2d.fillRect(doorX + 20, doorY + 30, 90, 180);
        g2d.fillRect(doorX + 130, doorY + 30, 90, 180);

        // Entrance step & Pillars
        g2d.setColor(new Color(190, 195, 205));
        g2d.fillRect(doorX - 40, bY + bH - 15, doorW + 80, 20);

        // Clean Vignette / Gradient at the bottom for smooth overlay
        GradientPaint bottomGrad = new GradientPaint(0, 1100, new Color(7, 14, 23, 0), 0, height, new Color(7, 14, 23, 240));
        g2d.setPaint(bottomGrad);
        g2d.fillRect(0, 1100, width, height - 1100);

        g2d.dispose();

        File output = new File("app/src/main/res/drawable/bg_kharsia_lobby_building.png");
        ImageIO.write(img, "PNG", output);
        System.out.println("Generated building image at: " + output.getAbsolutePath());
    }
}
