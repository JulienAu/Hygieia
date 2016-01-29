
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



function getProject(url) {
  $(document).ready(function() {
                var i=0;
                var cookieValue = document.cookie.replace(/(?:(?:^|.*;\s*)username\s*\=\s*([^;]*).*$)|^.*$/, "$1");                    
                var url2 = url+cookieValue;
                var id="";
                var idDashboard="";
                $.getJSON(url2, function(jd) {
                  $.each(jd, function(i, field){
                    idDashboard = field.id;
                    $.each(field, function(i, field){
                      if (i == "widgets"){
                        $.each(field, function(i, field){
                          if(field.componentId != id) {
                            id = field.componentId;
                            i += 1;
                            getjsonComparaison(id , idDashboard);
                            }    
                          });
                        }
                      });
                  });
              });
  });

}


function getjsonComparaison(id , idDashboard) {
  url = 'api/quality/static-analysis?componentId='+id+'&max=5';
  var array= new Array(10000);
  var array2 = new Array(10000);
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                var txt = "<tr><td><a href=\"#/dashboard/"+idDashboard+"\">"+jd.result[0].name+" </a></td><td>";
                var k = 0;
                $.each(jd, function(i, field){                  
                    if ( i == "result" ) {
                      for (var j = 0 ; j< field.length ; j++){
                        $.each(jd.result[j], function(i, field){
                          if ( i == "metrics" ) {
                          for (i = 0; i < field.length; i++) { 
                            switch(jd.result[j].metrics[i].name) {
                            case "line_coverage":
                                  k = 3;
                              break;
                            case "blocker_violations":
                                  k = 1;
                            break;
                            case "tests":
                                  k = 4;
                            break;
                            case "major_violations":
                                  k = 2;
                            break;
                             case "ncloc":
                                  k = 0;
                            break;
                            case "violations":
                                  k = 8;
                            break;
                            case "critical_violations":
                                  k = 7;
                            break;
                              default:
                              k = 8 + i;
                        } 
                        $.each(jd.result[j].metrics[i], function(i, field){
                          if(i == "value"){
                            if(array[k] >= 0){
                              if(field != array[k]){
                                if(!(array2[k] >= 0)){
                                  array2[k]=field;
                                }
                              }
                            }else{
                              array[k]=field;
                            }
                          }
                        });
                      }
                    
                    }
                  });
                }
                }
                });
                    getjsonJenkins('api/build/?componentId='+id+'&max=1' ,function(result) {
                     array[5]=result;
                     array[1] += array[7];
                     array[2] += array[8];
                     for (i = 7; i < 9; i++) {
                        if(!(array2[i]=== undefined)){
                            array2[i-6] += array2[i];
                          }
                        }
                      for (i = 0; i < 6; i++) {
                        if(array2[i]=== undefined){
                          txt += array[i]+"</td><td>";
                       } else {
                          if ((array[i] - array2[i]) > 0){
                            txt += array[i]+" <br /> (↑ + "+ Math.round((array[i] - array2[i])*100)/100+ ")</td><td>";
                          }else{
                          txt += array[i]+" <br /> (↓ "+ Math.round((array[i] - array2[i])*100)/100+ ")</td><td>";
                        }
                      }
                      }

                       if(txt != ""){
                        txt +="<a href ng-click=\"ctrl.deleteDashboard("+idDashboard+")\"><span class=\"fa fa-trash-o fa-lg text-danger\"></span></a></td></tr>";
                        $("#table").append(txt).removeClass("hidden");
                    }
                     });
                   });
              });

}


function getjsonJenkinsMetrics(url , callback){
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
