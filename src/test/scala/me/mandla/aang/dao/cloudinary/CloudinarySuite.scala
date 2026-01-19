package me.mandla.aang
package dao
package cloudinary

import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils
import munit.*

import scala.jdk.CollectionConverters.*

import java.util.Map

def cloudinary() =
  val cloudinary = Cloudinary("")
  cloudinary.config.secure = true;
  println(cloudinary.config.cloudName)
  cloudinary

def upload(imageUrl: String): Unit =
  try
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
  catch case e: Throwable => println(e.getMessage)

def getDetals(imageName: String): Unit =
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

final class CloudinarySpec extends FunSuite:
  test("can upload") {
    val imageUrl = "https://cloudinary-devs.github.io/cld-docs-assets/assets/images/coffee_cup.jpg"
    val imageName = "coffee_cup"

    val uploaded = upload(imageUrl)
    println(uploaded)

    val details = getDetals(imageName)
    println(details)
  }
