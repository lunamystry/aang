package me.mandla.aang
package dao
package cloudinary

import zio.{ ZIO, Task }

import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils

import scala.jdk.CollectionConverters.*

import java.util.Map

def cloudinary() =
  val url: String = java.lang.System.getenv("CLOUDINARY_URL")
  val cloudinary = Cloudinary(url)
  cloudinary.config.secure = true;
  cloudinary

def upload(imageUrl: String): Task[scala.collection.mutable.Map[String, Any]] =
  ZIO.attempt {
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
      .asInstanceOf[scala.collection.mutable.Map[String, Any]]
  }
