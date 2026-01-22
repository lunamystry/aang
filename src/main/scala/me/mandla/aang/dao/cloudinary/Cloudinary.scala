package me.mandla.aang
package dao
package cloudinary

import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils

import scala.jdk.CollectionConverters.*

import java.util.Map

def cloudinary() =
  val cloudinary = Cloudinary("")
  cloudinary.config.secure = true;
  println(cloudinary.config.cloudName)
  cloudinary

def upload(imageUrl: String): scala.collection.mutable.Map[String, Any] =
  try
    val res =
      cloudinary()
        .uploader()
        .upload(
          imageUrl,
          ObjectUtils.asMap(
            "use_filename",
            true,
            "unique_filename",
            false,
            "overwrite",
            true,
          ),
        )
        .asScala
        .asInstanceOf[scala.collection.mutable.Map[String, Any]];

    println("")
    println(res.get("secure_url"));
    res

  catch
    case e: Throwable =>
      println(e.getMessage);
      scala.collection.mutable.Map()

def getDetails(imageName: String): Unit =
  try
    println(
      cloudinary()
        .api()
        .resource(
          imageName,
          ObjectUtils.asMap(
            "quality_analysis",
            true,
          ),
        )
    )

  catch case e: Throwable => println(e.getMessage)
