package com.bofa.payment.score.service.Impl;

import com.bofa.payment.score.dao.ScoreMybatisDao;
import com.bofa.payment.score.pojo.Agent;
import com.bofa.payment.score.pojo.Score;
import com.bofa.payment.score.repository.AgentRepository;
import com.bofa.payment.score.service.ReportService;
import com.bofa.payment.score.utils.ExcelUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    private Log log = LogFactory.getLog(ReportServiceImpl.class);

    @Autowired
    AgentRepository agentDao;

    @Autowired
    ScoreMybatisDao scoreMybatisDao;

    @Override
    public void getMonthScoreReport(Integer year, Integer month, HttpServletResponse res) {
        String[] columeName = new String[]{"支付名稱","開發","重構","更新","協助","完成時間","說明","績效點數"};
        String[] monthArray = new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12"};
        List<Agent> agentList = agentDao.findByLevel(1);
        File file = null;
        Map totalPoint = new HashMap();
        try {
            //SpringBoot 打包成Jar之後 無法從jar檔裡面獲得Resource 只能從串流的方式獲取
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            InputStream fis = resourceLoader.getResource("templates/excel/ScoreTemp.xls").getInputStream();

            String path = "/home/agnusdei0717/";
            ExcelUtil.createDir(new File(path));

            String fileName = "績效-第三方支付組_"+year+"-"+monthArray[month]+".xls";
            File newFile = new File(path + fileName);

//            file = ResourceUtils.getFile("classpath:templates/excel/ScoreTemp.xls");
//            try (InputStream in = new BufferedInputStream(new FileInputStream(file));
            try (
//                    InputStream in = new BufferedInputStream(fis);
                OutputStream out = new FileOutputStream(newFile)) {
                HSSFWorkbook wb = new HSSFWorkbook(fis);
                HSSFSheet mainSheet = wb.getSheetAt(0);
                for(int a = 0;a<=agentList.size();a++) {
                    Agent agent;
                    List<Score> scoreList;

                    if(a < agentList.size()) {
                        agent = agentList.get(a);
                        scoreList = scoreMybatisDao.getScoreByYearAndMonthAndAgent(year, month, agent.getId());
                    }else{
                        agent = new Agent();
                        agent.setId(a+1);
                        agent.setCname("測試組");
                        scoreList = scoreMybatisDao.getScoreByYearAndMonthForQA(year, month);
                    }

                    HSSFSheet sheet = wb.cloneSheet(1);
                    String finalPoint = "0";
                    for(int i = 0;i<scoreList.size();i++) {
                        Score score = scoreList.get(i);
                        HSSFRow row = sheet.createRow(i + 1);
                        String status1 = score.getStatus1();
                        String status2 = score.getStatus2();
                        String status3 = score.getStatus3();
                        String status4 = score.getStatus4();
                        row.createCell(0).setCellValue(score.getPayName());
                        CellStyle style = wb.createCellStyle();
                        Cell cell;



                        if(score.getPayName().equals("總計:")) {
                            style.setBorderBottom((short) 0);
                            style.setBorderLeft((short) 0);
                            style.setBorderRight((short) 0);
                            style.setBorderTop((short) 0);
                        }else{
                            if ( status1 != null && status1.equals("新增")) {
                                style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
                            } else if (status2 != null && status2.equals("重構")) {
                                style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                            } else if (status3 != null && status3.equals("更新")) {
                                style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                            } else if (status4 != null && status4.equals("協助")) {
                                style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
                            }

                            style.setBorderBottom((short) 1);
                            style.setBorderLeft((short) 1);
                            style.setBorderRight((short) 1);
                            style.setBorderTop((short) 1);
                            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                        }

                        cell = row.createCell(1);
                        cell.setCellStyle(style);
                        cell.setCellValue(score.getStatus1());

                        cell = row.createCell(2);
                        cell.setCellStyle(style);
                        cell.setCellValue(score.getStatus2());

                        cell = row.createCell(3);
                        cell.setCellStyle(style);
                        cell.setCellValue(score.getStatus3());

                        cell = row.createCell(4);
                        cell.setCellStyle(style);
                        cell.setCellValue(score.getStatus4());

                        row.createCell(5).setCellValue(score.getEndDate());
                        row.createCell(6).setCellValue(score.getSituation());
                        row.createCell(7).setCellValue(score.getPoint());
                        finalPoint = score.getStatus1();
                    }
                    wb.setSheetName(agent.getId()+1,agent.getCname());
                    if(a < agentList.size()) {
                        mainSheet.getRow(agent.getId()).getCell(3).setCellValue(finalPoint);
                        mainSheet.getRow(agent.getId()).getCell(4).setCellValue(bonusLevel(finalPoint));
                    }
                }
                leaderBonus(mainSheet,agentList);
                wb.removeSheetAt(1);
                wb.setActiveSheet(0);
                wb.write(out);
                out.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            ExcelUtil.downloadFile(res, newFile);
            ExcelUtil.deleteFile(newFile);
        }catch (IOException e){
               log.info(e.toString());
        }

    }

    /**
     * 取得獎金等級
     * @param pointStr
     * @return
     */
    private String bonusLevel(String pointStr){
        String result = "0";
        if(pointStr != null && !pointStr.equals("")){
            int point = Integer.parseInt(pointStr);
            if(point >=7 && point <= 10){
                result = "2000";
            }else if(point >=11 && point <= 15){
                result = "5000";
            }else if(point >=16 && point <= 25){
                result = "8000";
            }else if(point >=26 && point <= 35){
                result = "12000";
            }else if(point >=36){
                result = "15000";
            }
        }
        return result;
    }

    /**
     * 取得組長加給等級
     * @param pointStr
     * @return
     */
    private int leaderBonusLevel(String pointStr){
        int result = 0;
        if(pointStr != null && !pointStr.equals("")){
            int point = Integer.parseInt(pointStr);
            if(point >=21 && point <= 25){
                result = 1;
            }else if(point >=26 && point <= 30){
                result = 2;
            }else if(point >=31){
                result = 3;
            }
        }
        return result;
    }


    /**
     * 處理最後組長獎金欄位
     * @param mainSheet
     * @param agentList
     */
    private void leaderBonus(HSSFSheet mainSheet, List<Agent> agentList){
        agentList.remove(0);
        String[] agentScores = new String[agentList.size()];
        int[] agentLevels = new int[agentList.size()];
        int total = 0;

        for(int i=0;i<agentList.size();i++){
            Agent agent = agentList.get(i);
            int agentLevel = leaderBonusLevel(mainSheet.getRow(agent.getId()).getCell(3).getStringCellValue());
            total += (agentLevel * 1500);
            int orginRow = 11 + 3 - agentLevel;

            String cellValue = mainSheet.getRow(orginRow).getCell(3).getStringCellValue();
            if(cellValue != null && !cellValue.equals("")){
                mainSheet.getRow(orginRow).getCell(3).setCellValue(String.valueOf(Integer.parseInt(cellValue)+1));
            }else{
                mainSheet.getRow(orginRow).getCell(3).setCellValue(String.valueOf(1));
            }
        }

        int leaderOriginBonus = Integer.parseInt(mainSheet.getRow(1).getCell(4).getStringCellValue());
        StringBuffer bonusStr = new StringBuffer();
        bonusStr.append(leaderOriginBonus + "(績效獎金)+\n");
        bonusStr.append(total+"(管理獎金)=\n").append((total+leaderOriginBonus));
        mainSheet.getRow(1).getCell(4).setCellValue(bonusStr.toString());
    }
}
