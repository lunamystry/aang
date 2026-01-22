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

final class CloudinarySpec extends FunSuite:
  test("can upload") {
    val imageUrl = "https://cloudinary-devs.github.io/cld-docs-assets/assets/images/coffee_cup.jpg"
    val imageName = "coffee_cup"

    val uploaded = upload(imageUrl)
    println(uploaded)

    val details = getDetails(imageName)
    println(details)
  }
