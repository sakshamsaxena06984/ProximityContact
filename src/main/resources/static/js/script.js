console.log("this is JS file")

const toggleSidebar= () => {
      if($(".sidebar").is(":visible")){
        //true condition
        console.log("inside the first block !!");
        $(".sidebar").css("display", "none")
        $(".content").css("margin-left", "0%");  
      }else{
        // false condition
        console.log("inside the another block");
        $(".sidebar").css("display", "block")
        $(".content").css("margin-left", "20%");
      }
};