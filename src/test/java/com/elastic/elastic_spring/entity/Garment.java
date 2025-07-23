package com.elastic.elastic_spring.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Document(indexName = "garments")
@Mapping(mappingPath = "index-mapping.json")
public class Garment {
	@Id
	private String id;
	private String name;
	private Integer price;
	private List<String> color;
	private List<String> size;
	private String material;
	private String brand;
	private String occasion;
	private String neckStyle;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the price
	 */
	public Integer getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(Integer price) {
		this.price = price;
	}

	/**
	 * @return the color
	 */
	public List<String> getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(List<String> color) {
		this.color = color;
	}

	/**
	 * @return the size
	 */
	public List<String> getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(List<String> size) {
		this.size = size;
	}

	/**
	 * @return the material
	 */
	public String getMaterial() {
		return material;
	}

	/**
	 * @param material the material to set
	 */
	public void setMaterial(String material) {
		this.material = material;
	}

	/**
	 * @return the brand
	 */
	public String getBrand() {
		return brand;
	}

	/**
	 * @param brand the brand to set
	 */
	public void setBrand(String brand) {
		this.brand = brand;
	}

	/**
	 * @return the occasion
	 */
	public String getOccasion() {
		return occasion;
	}

	/**
	 * @param occasion the occasion to set
	 */
	public void setOccasion(String occasion) {
		this.occasion = occasion;
	}

	/**
	 * @return the neckStyle
	 */
	public String getNeckStyle() {
		return neckStyle;
	}

	/**
	 * @param neckStyle the neckStyle to set
	 */
	public void setNeckStyle(String neckStyle) {
		this.neckStyle = neckStyle;
	}

	@Override
	public String toString() {
		return "Garment [id=" + id + ", name=" + name + ", price=" + price + ", color=" + color + ", size=" + size
				+ ", material=" + material + ", brand=" + brand + ", occasion=" + occasion + ", neckStyle=" + neckStyle + "]";
	}

}
