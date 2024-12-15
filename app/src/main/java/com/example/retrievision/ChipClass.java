package com.example.retrievision;
//for found object forms
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ChipClass implements Serializable {

private List<String> Category, Type, Color, Brand, Model, Text, Location;
private String Size, Width, Height, Shape, Remarks, Date, Time, StudentName, Status, ReportType, Image, reportId, UID, DateReported;
    public ChipClass() {

    }
    public ChipClass(String reportId) {
        this.reportId = reportId;
    }
    public void setReportId(String reportId){
        this.reportId = reportId;
    }
    public String getReportId(){
        return reportId;
    }
    public void setCategory(List<String> category){
        this.Category = category;
    }
    public List<String> getCategory(){
        return Category;
    }
    public void setType(List<String> type) {
        this.Type = type;
    }
    public List<String> getType() {
        return Type;
    }
    public void setColors(List<String> colors) {
        this.Color = colors;
    }
    public List<String> getColors() {
        return Color;
    }
    public void setBrand(List<String> brand) {
        this.Brand = brand;
    }
    public List<String> getBrand() {
        return Brand;
    }
    public void setModel(List<String> model) {
        this.Model = model;
    }
    public List<String> getModel() {
        return Model;
    }
    public void setText(List<String> text) {
        this.Text = text;
    }
    public List<String> getText() {
        return Text;
    }
    public void setLocation(List<String> location) {
        this.Location = location;
    }
    public List<String> getLocation() {
        return Location;
    }

    public void setSize(String size){
        this.Size = size;
    }
    public String getSize(){
        return Size;
    }

    public void setShape(String shape){
        this.Shape = shape;
    }
    public String getShape(){
        return Shape;
    }
    public void setWidth(String width){
        this.Width = width;
    }
    public String getWidth(){
        return Width;
    }
    public void setHeight(String height){
        this.Height = height;
    }
    public String getHeight(){
        return Height;
    }
    public void setRemarks(String remarks){
        this.Remarks = remarks;
    }
    public String getRemarks(){
        return Remarks;
    }
    public void setDate(String date){
        this.Date = date;
    }
    public String getDate(){
        return Date;
    }

    public void setTime(String time){
        this.Time = time;
    }
    public String getTime(){
        return Time;
    }

    public void setStudentName(String studentName){
        this.StudentName = studentName;
    }
    public String getStudentName(){
        return StudentName;
    }

    public void setUID(String uid){this.UID = uid;}
    public String getUID(){return UID;}

    public void setStatus(String status){
        this.Status = status;
    }
    public String getStatus(){
        return Status;
    }

    public void setReportType(String reportType){
        this.ReportType = reportType;
    }
    public String getReportType(){
        return ReportType;
    }

    public void setImageUrl(String image){
        this.Image = image;
    }
    public String getImageUrl(){
        return Image;
    }

    public void setDateReported(String dateReported) {
        this.DateReported = dateReported;
    }
    public String getDateReported(){return DateReported; }

}
