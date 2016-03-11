

function getjsonJenkins(url , callback){
  $(document).ready(function() {
    var array= new Array(10000);
                $.getJSON(url, function(jd) {
                  var k= 0;
                $.each(jd, function(i, field){
                  if ( i == "result" ) {
                        $.each(jd.result[0], function(i, field){
                            switch(i) {
                            case "buildStatus":
                                  array[0]=field;
                              break;
                            case "failCount":
                                 array[1]=field;
                            break;
                            case "skipCount":
                                  array[2]=field;
                            break;
                            case "passCount":
                                  array[3]=field;
                            break;
                             case "totalCount":
                                 array[4]=field;
                            break;
                              default:
                              k = 8 + i;
                        } 
                             
                              //$("#table").append(field)
                          });
                      callback(array);
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
                var title ="";
                $.getJSON(url2, function(jd) {
                  $.each(jd, function(i, field){
                    idDashboard = field.id;
                    title=field.title;
                    $.each(field, function(i, field){
                      if (i == "widgets"){
                        $.each(field, function(i, field){
                          if(field.componentId != id) {
                            id = field.componentId;
                            i += 1;
                            getjsonComparaison(id , idDashboard , title);
                            }    
                          });
                        }
                      });
                  });
              });
  });

}


function getjsonComparaison(id , idDashboard , title) {
  url = 'api/quality/static-analysis?componentId='+id+'&max=5';
  var array= new Array(10000);
  var array2 = new Array(10000);
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                var txt = "<tr><td><a href=\"#/dashboard/"+idDashboard+"\">"+title+" </a></td><td>";
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
                                  k = 11;
                            break;
                            case "critical_violations":
                                  k = 10;
                            break;
                              default:
                              k = 12 + i;
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
                     array[5]=result[0];
                     if(result[1] != "none"){
                      array[6]=result[1];
                      array[7]=result[2];
                      if(result[3] == "none"){
                        result[3]= parseInt(result[4]) - (parseInt(result[1]) + parseInt(result[2]));
                      }
                      array[8]=result[3];
                      array[9]=parseInt(result[3]) + parseInt(result[1]) + parseInt(result[2]);
                     }
                     array[1] += array[10];
                     array[2] += array[11];
                     for (i = 10; i < 12; i++) {
                        if(!(array2[i]=== undefined)){
                            array2[i-9] += array2[i];
                          }
                        }
                      for (i = 0; i < 10; i++) {
                        if(array2[i]=== undefined){
                          txt += array[i]+"</td><td>";
                       } else {
                          if ((array[i] - array2[i]) > 0){
                            txt += array[i]+" <br /> (↑+"+ Math.round((array[i] - array2[i])*100)/100+ ")</td><td>";
                          }else{
                          txt += array[i]+" <br /> (↓"+ Math.round((array[i] - array2[i])*100)/100+ ")</td><td>";
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

