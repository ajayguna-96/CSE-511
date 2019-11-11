package cse512

import org.apache.spark.sql.SparkSession

import scala.math.sqrt

object SpatialQuery extends App{
  def contains( queryRectangle:String, pointString:String ) : Boolean = {
    val rect = queryRectangle.split(",")
    val pt = pointString.split(",")
    val rectangle = rect.map(_.toDouble)
    val point = pt.map(_.toDouble) 
    if (point(0) >= rectangle(0) && point(1) >= rectangle(1) && point(0) <= rectangle(2) && point(1) <= rectangle(3) ){
        return true
    }
    return false
  }
  def within (point1:String, point2:String, distance:Double) : Boolean = {
    val p1 = point1.split(",")
    val pt1 = p1.map(_.toDouble)
    val p2 = point2.split(",")
    val pt2 = p2.map(_.toDouble)
    val x = (pt1(0)-pt2(0))*(pt1(0)-pt2(0))
    val y = (pt1(1)-pt2(1))*(pt1(1)-pt2(1))
    val dist = sqrt(x+y)
    if(dist <= distance)
      return true
    
    return false
  }
  def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((contains(queryRectangle , pointString))))

    val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((contains(queryRectangle , pointString))))

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((within(pointString1, pointString2, distance))))

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((within(pointString1, pointString2, distance))))
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
    resultDf.show()

    return resultDf.count()
  }
}
