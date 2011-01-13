#!/usr/bin/env ruby


categories = ["universities", "museums"]

ids = {}

categories.each do |category|

	lists = Dir::glob("input_fb_lists/#{category}/*.html")

	ids[category] = []

	lists.each do |list|
		open(list) do |inf|
			inf.read.scan(/SocialGraphManager\.init\("FanManager", "([^"]+)"\)/).each do |arr|
				ids[category] << arr[0]
			end
		end
	end
end

puts "<?xml version='1.0'?>
<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:foaf='http://xmlns.com/foaf/0.1/'
    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'
    xmlns:v='http://www.w3.org/2006/vcard/ns#'
    xmlns:og='http://ogp.me/ns#'>"


categories.each do |category|
	puts "<rdf:Description rdf:ID='#{category}'>"
	ids[category].each do |id|
		puts "  <foaf:topic>"
		puts "    <rdf:Description>"
		puts "      <og:id>#{id}</og:id>"
		puts "    </rdf:Description>"
		puts "  </foaf:topic>"
	end
	puts "</rdf:Description>"
end

puts "</rdf:RDF>"

