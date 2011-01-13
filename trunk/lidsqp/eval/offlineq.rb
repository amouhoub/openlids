#!/usr/bin/env ruby

open("allresults.txt") do |inp|

  inp.each_line do |line|

     fields = line[0..-2].split(/ /)
     entity = fields[-1]
     if entity != "person\n" && fields[0].to_i != 0
       fname = entity[8..-5].gsub(/\//,'.')

       if fname =~ /-dengler/
          fname = fname[1..-1]
       end
     
       open("tmpo.n3","w") do |tmpo|
         tmpo.write open("datasets/#{fname}1.n3").read
       end
       query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?p ?feature WHERE { <#{entity}> foaf:knows ?p . ?p foaf:based_near ?feature }" 



       execstr = "roqet -e \"#{query}\" -D tmpo.n3"

       out = `#{execstr}`
       puts "#{out.lines.count} #{line}"
     end

  end

end