function ConfJava(x , y)
{
  if(parseInt(y) < 0){
    if(parseInt(x) < Math.abs(parseInt(y))){
        return "<div id=\"moncercle\"></div>";
    } 
    return "";
  } else {
 if(parseInt(x) > parseInt(y)){
  return "<div id=\"moncercle\"></div>";
  }
  return "";
  }
}


function getjsonConfJava(url , callback){
  var array= new Array(101);
  for(i =0 ; i < 100 ; i++){
    array[i]=999999;
  }
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                  var k= 1;
                $.each(jd, function(i, field){
                  if ( i == "Conf") {
                    if(jd.Conf.length > 0){
                        $.each(jd.Conf[0], function(i, field){
                          if(k == 4 ){
                            array[k] = 999999;
                            array[k+1] = 999999;
                            array[k+2] = field;
                            k = k + 2;
                          } else {
                            array[k] = field;
                          }
                          k++;
                          });
                      }
                      callback(array);
                    }
                  });
              });
   });
}


function getProjectJava(url) {
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
                    name=field.application.name;
                    $.each(field, function(i, field){
                      if (i == "widgets"){
                        $.each(field, function(i, field){
                          if(field.componentId != id) {
                            id = field.componentId;
                            i += 1;
                            if(name == "java"){
                              getjsonComparaisonJava(id , idDashboard , title);
                            }
                            }    
                          });
                        }
                      });
                  });
              });
  });

}


function getjsonComparaisonJava(id , idDashboard , title) {
  url = 'api/quality/static-analysis?componentId='+id+'&max=5';
  var array= new Array(101);
  var array2 = new Array(101);
  for (i = 0; i < 100; i++) {
    array[i] = " - ";
  }
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                var txt = "<tr><td><a href=\"#/dashboard/"+idDashboard+"\">"+title+" </a></td>";
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
                                  k = 32;
                            break;
                            case "critical_violations":
                                  k = 31;
                            break;
                              default:
                              k = 100; //
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
                   getjsonConfJava('Conf.json' ,function(array3) {
                         array[1] += array[31];
                         array[2] += array[32];
                          k=-1;
                          var status = 0;
                          for (i = 0; i < 5; i++) {
                            k++;
                            var color = ConfJava(array[i] , array3[k]);
                            if(color.length > 0){
                              status=1;
                            }
                            txt += "<td>"+ color;
                            if(array2[i]=== undefined){
                              txt += array[i]+"</td>";
                            
                           } else {
                              if ((array[i] - array2[i]) > 0){
                                txt += array[i]+" <br /> (↑+"+ Math.round((array[i] - array2[i])*100)/100+ ")</td>";
                              }else{
                              txt += array[i]+" <br /> (↓"+ Math.round((array[i] - array2[i])*100)/100+ ")</td>";
                            }
                          }
                        }
                        if(status == 1){
                          txt += "<td><img src=\"status/nuage.png\"></td>";
                        }else{
                          txt += "<td><img src=\"status/soleil.png\"></td>";
                        }
                       if(txt != ""){
                        txt +="<td><a href ng-click=\"ctrl.deleteDashboard("+idDashboard+")\"><span class=\"fa fa-trash-o fa-lg text-danger\"></span></a></td></tr>";
                        $("#table1").append(txt).removeClass("hidden");
                        }
                    });
                });
          });

}



