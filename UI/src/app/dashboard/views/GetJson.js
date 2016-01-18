function getjsonJenkins(url , callback){
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                $.each(jd, function(i, field){
                  if ( i == "result" ) {
                        $.each(jd.result[0], function(i, field){
                            if ( i == "buildStatus" ) {
                               //$('table tr').eq(i).find('td').eq(7).append(field);
                             callback(field);
                              //$("#table").append(field)
                            }
                          });
                      }
                    });
              });
   });
}





function getjson(id , i) {
  url = 'api/quality/static-analysis?componentId='+id+'&max=1'
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                var array= new Array(10000);
                var txt = "<tr><td>";//<tr><td>"+jd.result[0].name+"</td><td>";
                var k = 0;
                $.each(jd, function(i, field){
                  
                    if ( i == "result" ) {
                        $.each(jd.result[0], function(i, field){
                            if ( i == "metrics" ) {
                        for (i = 0; i < field.length; i++) { 
                          switch(jd.result[0].metrics[i].name) {
                            case "line_coverage":
                                  k = 1;
                              break;
                            case "blocker_violations":
                                  k = 2;
                            break;
                            case "tests":
                                  k = 6;
                            break;
                            case "major_violations":
                                  k = 4;
                            break;
                             case "ncloc":
                                  k = 0;
                            break;
                            case "violations":
                                  k = 5;
                            break;
                            case "critical_violations":
                                  k = 3;
                            break;
                              default:
                              k = 8 + i;
                        } 
                        $.each(jd.result[0].metrics[i], function(i, field){
                          if(i == "value"){
                            array[k]=field;
                          }
                        });
                    }
                    }
                  });
                }
                
                });
                    getjsonJenkins('api/build/?componentId='+id+'&max=1' ,function(result) {
                     array[7]=result;
                      for (i = 0; i < 8; i++) { 
                        txt += array[i]+"</td><td>";
                      }

                       if(txt != ""){
                        $("#table").append(txt).removeClass("hidden");
                    }
                     });
                   });
              });

}

function getProject(url) {
  $(document).ready(function() {
                var i=0;
                var cookieValue = document.cookie.replace(/(?:(?:^|.*;\s*)username\s*\=\s*([^;]*).*$)|^.*$/, "$1");                    
                var url2 = url+cookieValue;
                var id="";
                $.getJSON(url2, function(jd) {
                  $.each(jd, function(i, field){
                    $.each(field, function(i, field){
                      if (i == "widgets"){
                        $.each(field, function(i, field){
                          if(field.componentId != id) {
                            id = field.componentId;
                            i += 1;
                            getjson(id);
                       }

                            });
                        }
                                       });
                                       });
              });
  });

}