export async function onRequest(context) {
  const { request } = context;
  const url = new URL(request.url);
  
  const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  };

  if (request.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  const pathname = url.pathname;
  
  if (pathname === '/api/health') {
    return new Response(JSON.stringify({ status: 'ok', timestamp: new Date().toISOString() }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    });
  }

  if (pathname === '/api/auth/login' && request.method === 'POST') {
    try {
      const body = await request.json();
      if (body.username === 'yaoyao' && body.password === '5201314') {
        const token = 'yaoyao_token_' + Date.now();
        return new Response(JSON.stringify({ 
          token,
          user: { id: 1, username: 'yaoyao', name: '瑶瑶' }
        }), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        });
      }
      return new Response(JSON.stringify({ message: '用户名或密码错误' }), {
        status: 401,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      });
    } catch (e) {
      return new Response(JSON.stringify({ message: '请求格式错误' }), {
        status: 400,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      });
    }
  }

  if (pathname === '/api/auth/me' && request.method === 'GET') {
    const auth = request.headers.get('Authorization');
    if (auth && auth.startsWith('Bearer ')) {
      return new Response(JSON.stringify({ 
        id: 1, 
        username: 'yaoyao', 
        name: '瑶瑶' 
      }), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      });
    }
    return new Response(JSON.stringify({ message: '未授权' }), {
      status: 401,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    });
  }

  return new Response(JSON.stringify({ message: '接口不存在', path: pathname }), {
    status: 404,
    headers: { ...corsHeaders, 'Content-Type': 'application/json' }
  });
}
