var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'activities',
                context: data.activities
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Activities',
                    activities:true,
                    breadcrumb:[
                        {link:'/', name:'Login',isLink:true},
                        {link:'/search.jag', name:'Search',isLink:true},
                        {link:'', name:'Activities',isLink:false}
                    ]
                }
            }
        ]
    });
};